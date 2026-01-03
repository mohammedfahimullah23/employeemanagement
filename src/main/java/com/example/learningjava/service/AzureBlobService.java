package com.example.learningjava.service;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class AzureBlobService {

    private final BlobContainerClient containerClient;

    public AzureBlobService(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    public String upload(MultipartFile file) {

        validate(file);

        String blobName = UUID.randomUUID() + "_" + sanitize(Objects.requireNonNull(file.getOriginalFilename()));

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            blobClient.setHttpHeaders(
                    new BlobHttpHeaders()
                            .setContentType(file.getContentType()));

        } catch (IOException e) {
            throw new RuntimeException("Azure Blob upload failed", e);
        }

        return blobName;
    }

    public void delete(String blobName) {
        try {
            containerClient.getBlobClient(blobName).deleteIfExists();
        } catch (Exception ex) {
            // log only — do not break business flow
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
