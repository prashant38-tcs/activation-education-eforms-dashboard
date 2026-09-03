package com.tcsion.eforms.controller;

import com.tcsion.eforms.repository.CustomerMasterRepository;
import com.tcsion.eforms.repository.StatusMasterRepository;
import com.tcsion.eforms.repository.UserRepository;
import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final CustomerMasterRepository customerMasterRepository;
    private final UserRepository userRepository;
    private final StatusMasterRepository statusMasterRepository;

    @GetMapping("/daily")
    public String daily(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         @RequestParam(required = false) Long customerId,
                         @RequestParam(required = false) Long developerId, Model model) {
        LocalDate target = date != null ? date : LocalDate.now();
        Map<String, Object> report = reportService.buildDailyReport(target, customerId, developerId);
        model.addAttribute("report", report);
        model.addAttribute("selectedDate", target);
        addFilters(model);
        return "reports/daily-report";
    }

    @GetMapping("/daily/export/excel")
    public ResponseEntity<ByteArrayResource> dailyExcel(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                         @RequestParam(required = false) Long customerId,
                                                         @RequestParam(required = false) Long developerId) {
        ByteArrayOutputStream out = reportService.exportDailyReportExcel(date, customerId, developerId);
        return excelResponse(out, "Daily_Report_" + date + ".xlsx");
    }

    @GetMapping("/weekly")
    public String weekly(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                          @RequestParam(required = false) Long customerId,
                          @RequestParam(required = false) Long developerId,
                          @RequestParam(required = false) Long statusId, Model model) {
        LocalDate start = weekStart != null ? weekStart : LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        Map<String, Object> report = reportService.buildWeeklyReport(start, end, customerId, developerId, statusId);
        model.addAttribute("report", report);
        addFilters(model);
        return "reports/weekly-report";
    }

    @GetMapping("/weekly/export/excel")
    public ResponseEntity<ByteArrayResource> weeklyExcel(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
                                                          @RequestParam(required = false) Long customerId,
                                                          @RequestParam(required = false) Long developerId,
                                                          @RequestParam(required = false) Long statusId) {
        LocalDate end = weekStart.plusDays(6);
        ByteArrayOutputStream out = reportService.exportWeeklyReportExcel(weekStart, end, customerId, developerId, statusId);
        return excelResponse(out, "Weekly_Report_" + weekStart + ".xlsx");
    }

    @GetMapping("/quarterly")
    public String quarterly(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer quarter, Model model) {
        int y = year != null ? year : LocalDate.now().getYear();
        int q = quarter != null ? quarter : ((LocalDate.now().getMonthValue() - 1) / 3) + 1;
        model.addAttribute("report", reportService.buildQuarterlyReport(y, q));
        return "reports/quarterly-report";
    }

    @GetMapping("/annual")
    public String annual(@RequestParam(required = false) Integer year, Model model) {
        int y = year != null ? year : LocalDate.now().getYear();
        model.addAttribute("report", reportService.buildAnnualReport(y));
        return "reports/annual-report";
    }

    private void addFilters(Model model) {
        model.addAttribute("customers", customerMasterRepository.findByActiveTrue());
        model.addAttribute("developers", userRepository.findByRoles_RoleCodeAndActiveTrue(Role.DEVELOPER));
        model.addAttribute("statuses", statusMasterRepository.findByActiveTrueOrderBySortOrderAsc());
    }

    private ResponseEntity<ByteArrayResource> excelResponse(ByteArrayOutputStream out, String filename) {
        ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
