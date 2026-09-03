package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.exception.BusinessValidationException;
import com.tcsion.eforms.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements com.tcsion.eforms.service.FileStorageService {

    @Value("${app.file-storage.root-location:./storage/attachments}")
    private String rootLocation;

    @Value("${app.file-storage.max-file-size-mb:10}")
    private long maxFileSizeMb;

    @Value("${app.file-storage.allowed-extensions:pdf,docx,xlsx,xls,png,jpg,jpeg,txt,sql}")
    private String allowedExtensionsCsv;

    private static final List<String> BLOCKED_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "msi", "com", "scr", "jar", "js", "vbs", "ps1", "dll", "app");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(rootLocation));
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize attachment storage location", e);
        }
    }

    @Override
    public void validateAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("A file must be selected for upload.");
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)) {
            throw new BusinessValidationException("The uploaded file must have a valid file extension.");
        }
        extension = extension.toLowerCase();
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new BusinessValidationException("Executable or script file types are not permitted as attachments.");
        }
        List<String> allowed = Arrays.asList(allowedExtensionsCsv.toLowerCase().split(","));
        if (!allowed.contains(extension)) {
            throw new BusinessValidationException(
                    "Unsupported attachment type '" + extension + "'. Allowed types: " + allowedExtensionsCsv);
        }
        long maxBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessValidationException("Attachment exceeds the maximum allowed size of " + maxFileSizeMb + " MB.");
        }
    }

    @Override
    public String store(MultipartFile file, String subFolder) {
        validateAttachment(file);
        try {
            Path targetDir = Paths.get(rootLocation, sanitizeSubFolder(subFolder));
            Files.createDirectories(targetDir);
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension.toLowerCase();
            Path targetPath = targetDir.resolve(storedFileName).normalize();
            if (!targetPath.startsWith(targetDir)) {
                throw new FileStorageException("Invalid file destination path.");
            }
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException e) {
            log.error("Failed to store uploaded attachment", e);
            throw new FileStorageException("Failed to store the uploaded file. Please try again.", e);
        }
    }

    @Override
    public InputStream retrieve(String storedFileName, String subFolder) {
        try {
            Path filePath = Paths.get(rootLocation, sanitizeSubFolder(subFolder), storedFileName).normalize();
            Path baseDir = Paths.get(rootLocation, sanitizeSubFolder(subFolder)).normalize();
            if (!filePath.startsWith(baseDir)) {
                throw new FileStorageException("Invalid file reference.");
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not read the requested file.", e);
        }
    }

    private String sanitizeSubFolder(String subFolder) {
        if (!StringUtils.hasText(subFolder)) return "misc";
        return subFolder.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
