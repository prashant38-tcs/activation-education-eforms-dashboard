package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.ImportBatch;
import com.tcsion.eforms.repository.ImportBatchRepository;
import com.tcsion.eforms.repository.ImportBatchRowRepository;
import com.tcsion.eforms.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/import")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEAM_LEAD','TECHNICAL_LEAD','DASHBOARD_HANDLER','SYSTEM_ADMIN')")
public class ImportController {

    private final ExcelImportService excelImportService;
    private final ImportBatchRepository importBatchRepository;
    private final ImportBatchRowRepository importBatchRowRepository;

    @GetMapping
    public String importHome(Model model) {
        model.addAttribute("recentBatches", importBatchRepository.findAllByOrderByUploadedAtDesc());
        return "import/import-home";
    }

    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        ByteArrayOutputStream out = excelImportService.generateImportTemplate();
        return excelResponse(out, "Ticket_Import_Template.xlsx");
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file, Model model) {
        ImportBatch batch = excelImportService.parseAndPreview(file);
        return "redirect:/import/" + batch.getId() + "/preview";
    }

    @GetMapping("/{batchId}/preview")
    public String preview(@PathVariable Long batchId, Model model) {
        ImportBatch batch = importBatchRepository.findById(batchId).orElseThrow();
        model.addAttribute("batch", batch);
        model.addAttribute("rows", importBatchRowRepository.findByBatch_IdOrderByRowNumberAsc(batchId));
        return "import/import-preview";
    }

    @PostMapping("/{batchId}/confirm")
    public String confirm(@PathVariable Long batchId) {
        excelImportService.confirmImport(batchId);
        return "redirect:/import/" + batchId + "/result";
    }

    @PostMapping("/{batchId}/cancel")
    public String cancel(@PathVariable Long batchId) {
        excelImportService.cancelImport(batchId);
        return "redirect:/import";
    }

    @GetMapping("/{batchId}/result")
    public String result(@PathVariable Long batchId, Model model) {
        ImportBatch batch = importBatchRepository.findById(batchId).orElseThrow();
        model.addAttribute("batch", batch);
        return "import/import-result";
    }

    @GetMapping("/{batchId}/errors")
    public ResponseEntity<ByteArrayResource> downloadErrors(@PathVariable Long batchId) {
        ByteArrayOutputStream out = excelImportService.exportRejectedRows(batchId);
        return excelResponse(out, "Import_Errors_Batch_" + batchId + ".xlsx");
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
