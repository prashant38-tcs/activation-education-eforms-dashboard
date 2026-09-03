package com.tcsion.eforms.service;

import com.tcsion.eforms.entity.ImportBatch;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;

public interface ExcelImportService {
    ImportBatch parseAndPreview(MultipartFile file);
    ImportBatch confirmImport(Long batchId);
    void cancelImport(Long batchId);
    ByteArrayOutputStream generateImportTemplate();
    ByteArrayOutputStream exportRejectedRows(Long batchId);
}
