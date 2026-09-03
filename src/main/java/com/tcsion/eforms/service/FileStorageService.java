package com.tcsion.eforms.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorageService {
    String store(MultipartFile file, String subFolder);
    InputStream retrieve(String storedFileName, String subFolder);
    void validateAttachment(MultipartFile file);
}
