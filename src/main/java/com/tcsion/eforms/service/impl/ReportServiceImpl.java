package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.entity.*;
import com.tcsion.eforms.repository.*;
import com.tcsion.eforms.service.ReportService;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.AuditService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Daily/Weekly/Quarterly/Annual reporting, built from ticket + activity
 * history rather than only the current ticket snapshot. All exports apply
 * current filters, stamp generated-by/date, and are recorded in the audit
 * log.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TicketRepository ticketRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final AuditService auditService;

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> buildDailyReport(LocalDate date, Long customerId, Long developerId) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<Ticket> allTickets = ticketRepository.findAll().stream()
                .filter(Ticket::isActive)
                .filter(t -> customerId == null || (t.getCustomer() != null && t.getCustomer().getId().equals(customerId)))
                .filter(t -> developerId == null || (t.getAssignedUser() != null && t.getAssignedUser().getId().equals(developerId)))
                .collect(Collectors.toList());

        List<Ticket> forwardedFromYesterday = allTickets.stream()
                .filter(t -> t.getCreatedDate().isBefore(dayStart) && t.isOpen())
                .collect(Collectors.toList());

        List<Ticket> openedToday = allTickets.stream()
                .filter(t -> !t.getCreatedDate().isBefore(dayStart) && t.getCreatedDate().isBefore(dayEnd))
                .collect(Collectors.toList());

        List<Ticket> onHoldToday = allTickets.stream()
                .filter(t -> t.isOnHold() && hasActivityOnDateToStatus(t, date, StatusMaster.ON_HOLD))
                .collect(Collectors.toList());

        List<Ticket> reassignedToday = allTickets.stream()
                .filter(t -> hasActivityOnDateToAnyStatus(t, date,
                        StatusMaster.UAT_IN_PROGRESS, StatusMaster.QA_IN_PROGRESS,
                        StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM, StatusMaster.REASSIGNED_TO_OTHER_TEAM))
                .collect(Collectors.toList());

        List<Ticket> closedToday = allTickets.stream()
                .filter(t -> t.getActualProductionDate() != null && t.getActualProductionDate().isEqual(date))
                .collect(Collectors.toList());

        List<Ticket> forwardedForNextDay = allTickets.stream()
                .filter(Ticket::isOpen)
                .collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportDate", date.format(DISPLAY_DATE));
        report.put("generatedBy", SecurityUtils.currentUsername());
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        report.put("forwardedFromYesterday", forwardedFromYesterday);
        report.put("openedToday", openedToday);
        report.put("onHoldToday", onHoldToday);
        report.put("reassignedToday", reassignedToday);
        report.put("closedToday", closedToday);
        report.put("forwardedForNextDay", forwardedForNextDay);
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> buildWeeklyReport(LocalDate weekStart, LocalDate weekEnd, Long customerId,
                                                  Long developerId, Long statusId) {
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(Ticket::isActive)
                .filter(t -> customerId == null || (t.getCustomer() != null && t.getCustomer().getId().equals(customerId)))
                .filter(t -> developerId == null || (t.getAssignedUser() != null && t.getAssignedUser().getId().equals(developerId)))
                .filter(t -> statusId == null || (t.getCurrentStatus() != null && t.getCurrentStatus().getId().equals(statusId)))
                .collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("weekStart", weekStart.format(DISPLAY_DATE));
        report.put("weekEnd", weekEnd.format(DISPLAY_DATE));
        report.put("generatedBy", SecurityUtils.currentUsername());
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        report.put("tickets", tickets);
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> buildQuarterlyReport(int year, int quarter) {
        LocalDate start = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate end = start.plusMonths(3);
        List<Ticket> tickets = ticketRepository.findAll().stream().filter(Ticket::isActive).collect(Collectors.toList());

        long newTickets = tickets.stream().filter(t -> !t.getCreatedDate().toLocalDate().isBefore(start)
                && t.getCreatedDate().toLocalDate().isBefore(end)).count();
        long movedToProduction = tickets.stream().filter(t -> t.getActualProductionDate() != null
                && !t.getActualProductionDate().isBefore(start) && t.getActualProductionDate().isBefore(end)).count();
        long openTickets = tickets.stream().filter(Ticket::isOpen).count();
        long onHold = tickets.stream().filter(Ticket::isOnHold).count();

        Map<String, Long> customerBreakdown = tickets.stream()
                .filter(t -> t.getCustomer() != null)
                .collect(Collectors.groupingBy(t -> t.getCustomer().getCustomerName(), Collectors.counting()));
        Map<String, Long> statusBreakdown = tickets.stream()
                .filter(t -> t.getCurrentStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getCurrentStatus().getDisplayName(), Collectors.counting()));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("year", year);
        report.put("quarter", quarter);
        report.put("newTickets", newTickets);
        report.put("movedToProduction", movedToProduction);
        report.put("openTickets", openTickets);
        report.put("onHoldTickets", onHold);
        report.put("customerBreakdown", customerBreakdown);
        report.put("statusBreakdown", statusBreakdown);
        report.put("generatedBy", SecurityUtils.currentUsername());
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> buildAnnualReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = start.plusYears(1);
        List<Ticket> tickets = ticketRepository.findAll().stream().filter(Ticket::isActive).collect(Collectors.toList());

        long totalVolume = tickets.stream().filter(t -> !t.getCreatedDate().toLocalDate().isBefore(start)
                && t.getCreatedDate().toLocalDate().isBefore(end)).count();
        long movedToProduction = tickets.stream().filter(t -> t.getActualProductionDate() != null
                && !t.getActualProductionDate().isBefore(start) && t.getActualProductionDate().isBefore(end)).count();
        long openPending = tickets.stream().filter(Ticket::isOpen).count();

        Map<Integer, Long> monthlyTrend = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            final int month = m;
            long count = tickets.stream().filter(t -> t.getCreatedDate().getYear() == year
                    && t.getCreatedDate().getMonthValue() == month).count();
            monthlyTrend.put(month, count);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("year", year);
        report.put("totalVolume", totalVolume);
        report.put("movedToProduction", movedToProduction);
        report.put("openPending", openPending);
        report.put("monthlyTrend", monthlyTrend);
        report.put("generatedBy", SecurityUtils.currentUsername());
        return report;
    }

    private boolean hasActivityOnDateToStatus(Ticket ticket, LocalDate date, String statusCode) {
        return ticketActivityRepository.findByTicket_IdOrderByActivityDatetimeDesc(ticket.getId()).stream()
                .anyMatch(a -> a.getActivityDatetime().toLocalDate().isEqual(date)
                        && a.getNewStatus() != null && statusCode.equals(a.getNewStatus().getStatusCode()));
    }

    private boolean hasActivityOnDateToAnyStatus(Ticket ticket, LocalDate date, String... statusCodes) {
        Set<String> codes = new HashSet<>(Arrays.asList(statusCodes));
        return ticketActivityRepository.findByTicket_IdOrderByActivityDatetimeDesc(ticket.getId()).stream()
                .anyMatch(a -> a.getActivityDatetime().toLocalDate().isEqual(date)
                        && a.getNewStatus() != null && codes.contains(a.getNewStatus().getStatusCode()));
    }

    @Override
    public ByteArrayOutputStream exportDailyReportExcel(LocalDate date, Long customerId, Long developerId) {
        Map<String, Object> report = buildDailyReport(date, customerId, developerId);
        ByteArrayOutputStream out = writeTicketListWorkbook(
                "Daily Report - " + date.format(DISPLAY_DATE),
                (List<Ticket>) report.get("forwardedForNextDay"));
        auditService.log("REPORT_DOWNLOADED", "REPORT", null, "DAILY_REPORT_" + date, null, "Excel export");
        return out;
    }

    @Override
    public ByteArrayOutputStream exportWeeklyReportExcel(LocalDate weekStart, LocalDate weekEnd, Long customerId,
                                                          Long developerId, Long statusId) {
        Map<String, Object> report = buildWeeklyReport(weekStart, weekEnd, customerId, developerId, statusId);
        ByteArrayOutputStream out = writeTicketListWorkbook(
                "Weekly Report - " + weekStart.format(DISPLAY_DATE) + " to " + weekEnd.format(DISPLAY_DATE),
                (List<Ticket>) report.get("tickets"));
        auditService.log("REPORT_DOWNLOADED", "REPORT", null, "WEEKLY_REPORT_" + weekStart, null, "Excel export");
        return out;
    }

    private ByteArrayOutputStream writeTicketListWorkbook(String title, List<Ticket> tickets) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sanitizeSheetName(title));
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue(title);
            Row metaRow = sheet.createRow(1);
            metaRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))
                    + " by " + SecurityUtils.currentUsername());

            String[] headers = {"Ticket Number", "CRM ID", "Customer", "Title", "Assigned Developer",
                    "Current Status", "Priority", "SLA State", "Risk Category", "Aging Days", "Estimated Production Date"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            sheet.createFreezePane(0, 4);

            int rowIdx = 4;
            for (Ticket t : tickets) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(sanitizeCell(t.getTicketNumber()));
                row.createCell(1).setCellValue(sanitizeCell(t.getCrmId()));
                row.createCell(2).setCellValue(t.getCustomer() != null ? sanitizeCell(t.getCustomer().getCustomerName()) : "");
                row.createCell(3).setCellValue(sanitizeCell(t.getTicketTitle()));
                row.createCell(4).setCellValue(t.getAssignedUser() != null ? sanitizeCell(t.getAssignedUser().getFullName()) : "Unassigned");
                row.createCell(5).setCellValue(t.getCurrentStatus() != null ? t.getCurrentStatus().getDisplayName() : "");
                row.createCell(6).setCellValue(t.getPriority() != null ? t.getPriority().getPriorityName() : "");
                row.createCell(7).setCellValue(t.getSlaState());
                row.createCell(8).setCellValue(t.getSlaRiskCategory());
                row.createCell(9).setCellValue(t.getAgingDays());
                row.createCell(10).setCellValue(t.getEstimatedProductionDate() != null ? t.getEstimatedProductionDate().format(DISPLAY_DATE) : "");
            }
            workbook.write(out);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private String sanitizeCell(String value) {
        if (value == null) return "";
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            return "'" + value;
        }
        return value;
    }

    private String sanitizeSheetName(String name) {
        String cleaned = name.replaceAll("[\\\\/*\\[\\]:?]", "-");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    @Override
    public ByteArrayOutputStream exportReportCsv(Map<String, Object> reportData, String reportTitle) {
        StringBuilder sb = new StringBuilder();
        sb.append(reportTitle).append("\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")))
                .append(" by ").append(SecurityUtils.currentUsername()).append("\n\n");

        Object ticketsObj = reportData.get("tickets") != null ? reportData.get("tickets") : reportData.get("forwardedForNextDay");
        if (ticketsObj instanceof List) {
            sb.append("Ticket Number,CRM ID,Customer,Title,Assigned Developer,Current Status,Priority,SLA State,Risk Category,Aging Days\n");
            for (Object o : (List<?>) ticketsObj) {
                Ticket t = (Ticket) o;
                sb.append(csv(t.getTicketNumber())).append(",")
                        .append(csv(t.getCrmId())).append(",")
                        .append(csv(t.getCustomer() != null ? t.getCustomer().getCustomerName() : "")).append(",")
                        .append(csv(t.getTicketTitle())).append(",")
                        .append(csv(t.getAssignedUser() != null ? t.getAssignedUser().getFullName() : "Unassigned")).append(",")
                        .append(csv(t.getCurrentStatus() != null ? t.getCurrentStatus().getDisplayName() : "")).append(",")
                        .append(csv(t.getPriority() != null ? t.getPriority().getPriorityName() : "")).append(",")
                        .append(csv(t.getSlaState())).append(",")
                        .append(csv(t.getSlaRiskCategory())).append(",")
                        .append(t.getAgingDays()).append("\n");
            }
        }
        auditService.log("REPORT_DOWNLOADED", "REPORT", null, reportTitle, null, "CSV export");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try { out.write(sb.toString().getBytes("UTF-8")); } catch (IOException ignored) { }
        return out;
    }

    private String csv(String value) {
        String v = sanitizeCell(value == null ? "" : value);
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    @Override
    public ByteArrayOutputStream exportReportPdf(Map<String, Object> reportData, String reportTitle) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font metaFont = new Font(Font.HELVETICA, 9, Font.ITALIC);
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

            Paragraph title = new Paragraph(reportTitle, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph meta = new Paragraph("Generated: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))
                    + " by " + SecurityUtils.currentUsername(), metaFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(12);
            document.add(meta);

            Object ticketsObj = reportData.get("tickets") != null ? reportData.get("tickets") : reportData.get("forwardedForNextDay");
            if (ticketsObj instanceof List) {
                String[] headers = {"Ticket #", "Customer", "Title", "Developer", "Status", "Priority", "SLA", "Risk", "Aging"};
                PdfPTable table = new PdfPTable(headers.length);
                table.setWidthPercentage(100);
                for (String h : headers) table.addCell(new PdfPCell(new Phrase(h, headerFont)));
                for (Object o : (List<?>) ticketsObj) {
                    Ticket t = (Ticket) o;
                    table.addCell(new Phrase(nullSafe(t.getTicketNumber()), cellFont));
                    table.addCell(new Phrase(t.getCustomer() != null ? nullSafe(t.getCustomer().getCustomerName()) : "", cellFont));
                    table.addCell(new Phrase(nullSafe(t.getTicketTitle()), cellFont));
                    table.addCell(new Phrase(t.getAssignedUser() != null ? nullSafe(t.getAssignedUser().getFullName()) : "Unassigned", cellFont));
                    table.addCell(new Phrase(t.getCurrentStatus() != null ? t.getCurrentStatus().getDisplayName() : "", cellFont));
                    table.addCell(new Phrase(t.getPriority() != null ? t.getPriority().getPriorityName() : "", cellFont));
                    table.addCell(new Phrase(nullSafe(t.getSlaState()), cellFont));
                    table.addCell(new Phrase(nullSafe(t.getSlaRiskCategory()), cellFont));
                    table.addCell(new Phrase(String.valueOf(t.getAgingDays()), cellFont));
                }
                document.add(table);
            }
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
        auditService.log("REPORT_DOWNLOADED", "REPORT", null, reportTitle, null, "PDF export");
        return out;
    }

    private String nullSafe(String value) { return value == null ? "" : value; }
}
