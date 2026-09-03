package com.tcsion.eforms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcsion.eforms.entity.*;
import com.tcsion.eforms.exception.BusinessValidationException;
import com.tcsion.eforms.exception.ResourceNotFoundException;
import com.tcsion.eforms.repository.*;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.AgingService;
import com.tcsion.eforms.service.AuditService;
import com.tcsion.eforms.service.ExcelImportService;
import com.tcsion.eforms.service.NotificationService;
import com.tcsion.eforms.service.SlaRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Excel ticket import pipeline (Section 6). Two-phase design: preview
 * classifies every row and persists it with PENDING_PREVIEW status without
 * touching tickets; confirmImport() applies inserts/updates transactionally.
 * Developer-entered technical details/remarks/activity history are never
 * overwritten by blank Excel values.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportServiceImpl implements ExcelImportService {

    private static final String[] REQUIRED_HEADERS = {
            "Ticket Number", "CRM ID", "Customer Name", "Customer Category", "Ticket Title",
            "Ticket Description", "Ticket Type", "Priority", "Severity", "Assigned User",
            "Assignment Date", "Created Date", "Expected Closure Date", "Estimated Production Date",
            "Current Status", "Source Team", "Dependency Team", "Existing Remark"
    };

    private final ImportBatchRepository importBatchRepository;
    private final ImportBatchRowRepository importBatchRowRepository;
    private final TicketRepository ticketRepository;
    private final CustomerMasterRepository customerMasterRepository;
    private final TicketTypeMasterRepository ticketTypeMasterRepository;
    private final PriorityMasterRepository priorityMasterRepository;
    private final SeverityMasterRepository severityMasterRepository;
    private final StatusMasterRepository statusMasterRepository;
    private final TeamMasterRepository teamMasterRepository;
    private final UserRepository userRepository;
    private final TicketAssignmentRepository ticketAssignmentRepository;
    private final TicketActivityRepository ticketActivityRepository;

    private final AuditService auditService;
    private final NotificationService notificationService;
    private final AgingService agingService;
    private final SlaRiskService slaRiskService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("dd-MM-yyyy"), DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"), DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    };

    @Override
    @Transactional
    public ImportBatch parseAndPreview(MultipartFile file) {
        validateFile(file);
        String checksum;
        try {
            checksum = DigestUtils.sha256Hex(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessValidationException("Could not read the uploaded file.");
        }

        User uploader = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessValidationException("Authentication is required to import tickets."));

        ImportBatch batch = ImportBatch.builder()
                .originalFileName(file.getOriginalFilename())
                .checksum(checksum)
                .uploadedBy(uploader)
                .processingStatus(ImportBatch.PENDING_PREVIEW)
                .build();
        batch = importBatchRepository.save(batch);

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(sheet);

            Set<String> seenTicketNumbersInFile = new HashSet<>();
            int rowCount = 0, invalidCount = 0, duplicateCount = 0, newCount = 0, changedCount = 0, unchangedCount = 0;

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowBlank(row)) continue;
                rowCount++;

                Map<String, String> data = extractRowData(row);
                String ticketNumber = StringUtils.trimToNull(data.get("Ticket Number"));
                String crmId = StringUtils.trimToNull(data.get("CRM ID"));
                List<String> errors = new ArrayList<>();

                if (StringUtils.isBlank(ticketNumber) && StringUtils.isBlank(crmId)) {
                    errors.add("Either Ticket Number or CRM ID is required.");
                }
                if (StringUtils.isBlank(data.get("Ticket Title"))) {
                    errors.add("Ticket Title is required.");
                }
                if (StringUtils.isBlank(data.get("Customer Name"))) {
                    errors.add("Customer Name is required.");
                }
                String assignedUsername = StringUtils.trimToNull(data.get("Assigned User"));
                if (assignedUsername != null) {
                    Optional<User> assignedUserOpt = userRepository.findByUsernameIgnoreCase(assignedUsername);
                    if (!assignedUserOpt.isPresent() || !assignedUserOpt.get().isActive()) {
                        errors.add("Assigned User '" + assignedUsername + "' is not a known active application user.");
                    }
                }
                parseDateSafely(data.get("Assignment Date"), errors, "Assignment Date");
                parseDateSafely(data.get("Expected Closure Date"), errors, "Expected Closure Date");
                parseDateSafely(data.get("Estimated Production Date"), errors, "Estimated Production Date");

                String classification;
                if (ticketNumber != null && seenTicketNumbersInFile.contains(ticketNumber)) {
                    classification = ImportBatchRow.DUPLICATE_IN_FILE;
                    errors.add("Duplicate Ticket Number within the uploaded file.");
                    duplicateCount++;
                } else if (!errors.isEmpty()) {
                    classification = ImportBatchRow.INVALID;
                    invalidCount++;
                } else {
                    Optional<Ticket> existing = ticketNumber != null
                            ? ticketRepository.findByTicketNumberIgnoreCase(ticketNumber)
                            : ticketRepository.findByCrmIdIgnoreCase(crmId);
                    if (existing.isPresent()) {
                        boolean changed = hasChanges(existing.get(), data);
                        classification = changed ? ImportBatchRow.EXISTING_CHANGED : ImportBatchRow.EXISTING_UNCHANGED;
                        if (changed) changedCount++; else unchangedCount++;
                    } else {
                        classification = ImportBatchRow.NEW;
                        newCount++;
                    }
                }
                if (ticketNumber != null) seenTicketNumbersInFile.add(ticketNumber);

                String rawJson;
                try { rawJson = objectMapper.writeValueAsString(data); }
                catch (Exception e) { rawJson = "{}"; }

                ImportBatchRow batchRow = ImportBatchRow.builder()
                        .batch(batch).rowNumber(rowIdx + 1)
                        .ticketNumber(ticketNumber).crmId(crmId)
                        .rawDataJson(rawJson)
                        .rowClassification(classification)
                        .errorReason(errors.isEmpty() ? null : String.join(" | ", errors))
                        .build();
                importBatchRowRepository.save(batchRow);
            }

            batch.setRowCount(rowCount);
            batch.setRejectedCount(invalidCount);
            batch.setDuplicateCount(duplicateCount);
            batch.setProcessingResult(String.format(
                    "New: %d | Changed: %d | Unchanged: %d | Invalid: %d | Duplicate-in-file: %d",
                    newCount, changedCount, unchangedCount, invalidCount, duplicateCount));
            importBatchRepository.save(batch);

        } catch (IOException e) {
            batch.setProcessingStatus(ImportBatch.FAILED);
            batch.setProcessingResult("Failed to parse the workbook: " + e.getMessage());
            importBatchRepository.save(batch);
            log.error("Excel parse failure for batch {}", batch.getId(), e);
            throw new BusinessValidationException("The uploaded file could not be parsed. Please verify it is a valid .xlsx file.");
        }

        auditService.log("EXCEL_UPLOAD_PREVIEWED", "IMPORT_BATCH", batch.getId(), batch.getOriginalFileName(),
                null, batch.getProcessingResult());
        return batch;
    }

    @Override
    @Transactional
    public ImportBatch confirmImport(Long batchId) {
        ImportBatch batch = importBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found."));
        if (!ImportBatch.PENDING_PREVIEW.equals(batch.getProcessingStatus())) {
            throw new BusinessValidationException("This import batch has already been processed or cancelled.");
        }

        List<ImportBatchRow> rows = importBatchRowRepository.findByBatch_IdOrderByRowNumberAsc(batchId);
        int inserted = 0, updated = 0;
        StatusMaster defaultStatus = statusMasterRepository.findByStatusCode(StatusMaster.NEW)
                .orElseThrow(() -> new ResourceNotFoundException("Default status configuration missing."));

        for (ImportBatchRow row : rows) {
            if (!ImportBatchRow.NEW.equals(row.getRowClassification())
                    && !ImportBatchRow.EXISTING_CHANGED.equals(row.getRowClassification())) {
                continue;
            }
            try {
                Map<String, String> data = objectMapper.readValue(row.getRawDataJson(), Map.class);
                Ticket ticket = applyRowToTicket(data, defaultStatus, batch.getUploadedBy());
                row.setResultingTicket(ticket);
                row.setProcessed(true);
                if (ImportBatchRow.NEW.equals(row.getRowClassification())) inserted++; else updated++;
            } catch (Exception e) {
                row.setRowClassification(ImportBatchRow.INVALID);
                row.setErrorReason("Processing failure during commit: " + e.getMessage());
                log.error("Row {} of batch {} failed during commit", row.getRowNumber(), batchId, e);
            }
            importBatchRowRepository.save(row);
        }

        batch.setInsertedCount(inserted);
        batch.setUpdatedCount(updated);
        batch.setProcessingStatus(ImportBatch.COMPLETED);
        batch.setCommittedAt(LocalDateTime.now());
        batch = importBatchRepository.save(batch);

        notificationService.notify(batch.getUploadedBy(), Notification.IMPORT_COMPLETED,
                "Import completed: " + batch.getOriginalFileName(),
                String.format("Inserted %d, Updated %d, Rejected %d.", inserted, updated, batch.getRejectedCount()),
                null);
        auditService.log("EXCEL_IMPORT_CONFIRMED", "IMPORT_BATCH", batch.getId(), batch.getOriginalFileName(),
                null, "Inserted=" + inserted + ";Updated=" + updated);
        return batch;
    }

    @Override
    @Transactional
    public void cancelImport(Long batchId) {
        ImportBatch batch = importBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found."));
        batch.setProcessingStatus(ImportBatch.CANCELLED);
        importBatchRepository.save(batch);
        auditService.log("EXCEL_IMPORT_CANCELLED", "IMPORT_BATCH", batch.getId(), batch.getOriginalFileName(), null, null);
    }

    private Ticket applyRowToTicket(Map<String, String> data, StatusMaster defaultStatus, User uploader) {
        String ticketNumber = StringUtils.trimToNull(data.get("Ticket Number"));
        String crmId = StringUtils.trimToNull(data.get("CRM ID"));

        Optional<Ticket> existingOpt = ticketNumber != null
                ? ticketRepository.findByTicketNumberIgnoreCase(ticketNumber)
                : ticketRepository.findByCrmIdIgnoreCase(crmId);

        Ticket ticket = existingOpt.orElseGet(Ticket::new);
        boolean isNew = !existingOpt.isPresent();

        CustomerMaster customer = customerMasterRepository.findByCustomerNameIgnoreCase(data.get("Customer Name"))
                .orElseGet(() -> customerMasterRepository.save(CustomerMaster.builder()
                        .customerName(data.get("Customer Name"))
                        .customerCategory(data.get("Customer Category"))
                        .build()));

        User previousAssignee = ticket.getAssignedUser();
        String assignedUsername = StringUtils.trimToNull(data.get("Assigned User"));
        User assignedUser = assignedUsername != null
                ? userRepository.findByUsernameIgnoreCase(assignedUsername).orElse(null) : ticket.getAssignedUser();

        if (isNew) {
            ticket.setTicketNumber(ticketNumber != null ? ticketNumber : "CRM-" + crmId);
            ticket.setCrmId(crmId);
            ticket.setCurrentStatus(defaultStatus);
        }
        ticket.setCustomer(customer);
        if (StringUtils.isNotBlank(data.get("Ticket Title"))) {
            ticket.setTicketTitle(data.get("Ticket Title"));
        } else if (ticket.getTicketTitle() == null) {
            ticket.setTicketTitle("Imported Ticket " + ticket.getTicketNumber());
        }
        if (StringUtils.isNotBlank(data.get("Ticket Description"))) {
            ticket.setTicketDescription(data.get("Ticket Description"));
        }
        applyTypeIfPresent(data.get("Ticket Type"), ticket);
        applyPriorityIfPresent(data.get("Priority"), ticket);
        applySeverityIfPresent(data.get("Severity"), ticket);
        applyTeamsIfPresent(data.get("Source Team"), data.get("Dependency Team"), ticket);

        ticket.setAssignedUser(assignedUser);
        if (assignedUser != null && (previousAssignee == null || !previousAssignee.getId().equals(assignedUser.getId()))) {
            ticket.setPreviousAssignee(previousAssignee);
            ticket.setAssignmentDate(LocalDateTime.now());
        }

        LocalDate expectedClosure = parseDateSafely(data.get("Expected Closure Date"), new ArrayList<>(), "Expected Closure Date");
        LocalDate estimatedProduction = parseDateSafely(data.get("Estimated Production Date"), new ArrayList<>(), "Estimated Production Date");
        if (expectedClosure != null) ticket.setExpectedClosureDate(expectedClosure);
        if (estimatedProduction != null) ticket.setEstimatedProductionDate(estimatedProduction);

        ticket = ticketRepository.save(ticket);

        recordImportAssignment(ticket, previousAssignee, assignedUser, uploader);

        String existingRemark = StringUtils.trimToNull(data.get("Existing Remark"));
        if (existingRemark != null) {
            TicketActivity activity = TicketActivity.builder()
                    .ticket(ticket).previousStatus(ticket.getCurrentStatus()).newStatus(ticket.getCurrentStatus())
                    .workSummary("Imported from Excel")
                    .detailedRemark(existingRemark)
                    .updatedBy(uploader)
                    .source(TicketActivity.SOURCE_EXCEL_IMPORT)
                    .build();
            ticketActivityRepository.save(activity);
        }

        agingService.recalculateAging(ticket);
        slaRiskService.recalculateRisk(ticket);

        if (assignedUser != null) {
            notificationService.notify(assignedUser, Notification.NEW_TICKET_ASSIGNED,
                    "Ticket imported/assigned: " + ticket.getTicketNumber(),
                    "Ticket " + ticket.getTicketNumber() + " has been assigned to you via Excel import.", ticket);
        }
        return ticket;
    }

    private void recordImportAssignment(Ticket ticket, User from, User to, User importedBy) {
        if (to == null || (from != null && from.getId().equals(to.getId()))) return;
        TicketAssignment assignment = TicketAssignment.builder()
                .ticket(ticket).assignedFromUser(from).assignedToUser(to)
                .assignedBy(importedBy).assignmentType(TicketAssignment.IMPORT)
                .reason("Assigned via Excel import")
                .build();
        ticketAssignmentRepository.save(assignment);
    }

    private void applyTypeIfPresent(String typeName, Ticket ticket) {
        if (StringUtils.isBlank(typeName)) return;
        ticketTypeMasterRepository.findByTypeCode(normalizeCode(typeName)).ifPresent(ticket::setTicketType);
    }
    private void applyPriorityIfPresent(String priorityName, Ticket ticket) {
        if (StringUtils.isBlank(priorityName)) return;
        priorityMasterRepository.findByPriorityCode(normalizeCode(priorityName)).ifPresent(ticket::setPriority);
    }
    private void applySeverityIfPresent(String severityName, Ticket ticket) {
        if (StringUtils.isBlank(severityName)) return;
        severityMasterRepository.findBySeverityCode(normalizeCode(severityName)).ifPresent(ticket::setSeverity);
    }
    private void applyTeamsIfPresent(String sourceTeamName, String dependencyTeamName, Ticket ticket) {
        if (StringUtils.isNotBlank(sourceTeamName)) {
            teamMasterRepository.findByTeamCode(normalizeCode(sourceTeamName)).ifPresent(ticket::setSourceTeam);
        }
        if (StringUtils.isNotBlank(dependencyTeamName)) {
            teamMasterRepository.findByTeamCode(normalizeCode(dependencyTeamName)).ifPresent(ticket::setDependencyTeam);
        }
    }
    private String normalizeCode(String value) {
        return value.trim().toUpperCase().replaceAll("[\\s-]+", "_");
    }

    private boolean hasChanges(Ticket existing, Map<String, String> data) {
        if (StringUtils.isNotBlank(data.get("Ticket Title")) && !data.get("Ticket Title").equals(existing.getTicketTitle())) {
            return true;
        }
        String assignedUsername = StringUtils.trimToNull(data.get("Assigned User"));
        if (assignedUsername != null && (existing.getAssignedUser() == null
                || !assignedUsername.equalsIgnoreCase(existing.getAssignedUser().getUsername()))) {
            return true;
        }
        String currentStatusIncoming = StringUtils.trimToNull(data.get("Current Status"));
        if (currentStatusIncoming != null && existing.getCurrentStatus() != null
                && !normalizeCode(currentStatusIncoming).equals(existing.getCurrentStatus().getStatusCode())) {
            return true;
        }
        return false;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("Please select an Excel file to upload.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessValidationException("Only .xlsx or .xls files are supported for ticket import.");
        }
        String contentType = file.getContentType();
        boolean validContentType = contentType != null && (
                contentType.contains("spreadsheet") || contentType.contains("excel") || contentType.equals("application/octet-stream"));
        if (!validContentType) {
            throw new BusinessValidationException("The uploaded file does not appear to be a valid Excel file.");
        }
        long maxBytes = 10L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessValidationException("Import file exceeds the maximum allowed size of 10 MB.");
        }
    }

    private void validateHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new BusinessValidationException("The uploaded file does not contain a header row.");
        }
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String value = cell == null ? "" : cell.getStringCellValue().trim();
            if (!REQUIRED_HEADERS[i].equalsIgnoreCase(value)) missing.add(REQUIRED_HEADERS[i]);
        }
        if (!missing.isEmpty()) {
            throw new BusinessValidationException(
                    "The uploaded file is missing or has misordered required column(s): " + String.join(", ", missing)
                    + ". Please use the provided import template.");
        }
    }

    private boolean isRowBlank(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.isNotBlank(getCellAsString(cell))) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> extractRowData(Row row) {
        Map<String, String> data = new LinkedHashMap<>();
        for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            data.put(REQUIRED_HEADERS[i], StringUtils.trimToEmpty(getCellAsString(cell)));
        }
        return data;
    }

    private String getCellAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                }
                double d = cell.getNumericCellValue();
                return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private LocalDate parseDateSafely(String value, List<String> errors, String fieldLabel) {
        if (StringUtils.isBlank(value)) return null;
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try { return LocalDate.parse(value.trim(), fmt); }
            catch (Exception ignored) { }
        }
        errors.add("Invalid date format for " + fieldLabel + ": '" + value + "'.");
        return null;
    }

    @Override
    public ByteArrayOutputStream generateImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Ticket Import Template");
            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(REQUIRED_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 22 * 256);
            }
            sheet.createFreezePane(0, 1);

            Row example = sheet.createRow(1);
            String[] sample = {"EF-TICKET-0001", "CRM-100234", "Sample University", "Premium",
                    "Sample ticket title", "Sample description of the requirement", "BUG", "P2_HIGH",
                    "SEV2_MAJOR", "jdoe", "01-09-2026", "01-09-2026", "10-09-2026", "15-09-2026",
                    "WORK_IN_PROGRESS", "AE_EFORMS", "FRAMEWORK", "Example remark"};
            for (int i = 0; i < sample.length; i++) example.createCell(i).setCellValue(sample[i]);
            workbook.write(out);
            return out;
        } catch (IOException e) {
            throw new BusinessValidationException("Failed to generate the import template.");
        }
    }

    @Override
    public ByteArrayOutputStream exportRejectedRows(Long batchId) {
        List<ImportBatchRow> rejectedRows = importBatchRowRepository.findByBatch_IdAndRowClassification(batchId, ImportBatchRow.INVALID);
        rejectedRows.addAll(importBatchRowRepository.findByBatch_IdAndRowClassification(batchId, ImportBatchRow.DUPLICATE_IN_FILE));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Rejected Rows");
            Row header = sheet.createRow(0);
            String[] cols = Arrays.copyOf(REQUIRED_HEADERS, REQUIRED_HEADERS.length + 1);
            cols[REQUIRED_HEADERS.length] = "Error Reason";
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            sheet.createFreezePane(0, 1);

            int rowIdx = 1;
            for (ImportBatchRow row : rejectedRows) {
                Row excelRow = sheet.createRow(rowIdx++);
                try {
                    Map<String, String> data = objectMapper.readValue(row.getRawDataJson(), Map.class);
                    for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
                        excelRow.createCell(i).setCellValue(data.getOrDefault(REQUIRED_HEADERS[i], ""));
                    }
                } catch (Exception ignored) { }
                excelRow.createCell(REQUIRED_HEADERS.length).setCellValue(row.getErrorReason());
            }
            workbook.write(out);
            return out;
        } catch (IOException e) {
            throw new BusinessValidationException("Failed to generate the rejected rows export.");
        }
    }
}
