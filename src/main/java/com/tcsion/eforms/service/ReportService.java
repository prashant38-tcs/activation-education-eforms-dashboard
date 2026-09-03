package com.tcsion.eforms.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Map;

public interface ReportService {
    Map<String, Object> buildDailyReport(LocalDate date, Long customerId, Long developerId);
    Map<String, Object> buildWeeklyReport(LocalDate weekStart, LocalDate weekEnd, Long customerId, Long developerId, Long statusId);
    Map<String, Object> buildQuarterlyReport(int year, int quarter);
    Map<String, Object> buildAnnualReport(int year);
    ByteArrayOutputStream exportDailyReportExcel(LocalDate date, Long customerId, Long developerId);
    ByteArrayOutputStream exportWeeklyReportExcel(LocalDate weekStart, LocalDate weekEnd, Long customerId, Long developerId, Long statusId);
    ByteArrayOutputStream exportReportCsv(Map<String, Object> reportData, String reportTitle);
    ByteArrayOutputStream exportReportPdf(Map<String, Object> reportData, String reportTitle);
}
