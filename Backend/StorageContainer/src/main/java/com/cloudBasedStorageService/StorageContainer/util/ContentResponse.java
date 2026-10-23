package com.cloudBasedStorageService.StorageContainer.util;

import com.cloudBasedStorageService.StorageContainer.model.dto.FileDownloadResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared by the authenticated and the anonymous download endpoints so a stored file
 * name can only ever be sanitised in one place.
 */
public final class ContentResponse {

    private ContentResponse() {
    }

    /**
     * Returned as a Resource so Spring streams it in constant memory and answers
     * Range requests, which is what lets video and PDF previews seek into a file
     * instead of downloading it first.
     */
    public static ResponseEntity<Resource> of(FileDownloadResponse file, boolean asAttachment) {

        Path path = file.path();

        if (!Files.isReadable(path)) {
            throw new ResponseStatusException(
                    HttpStatus.GONE, "This file is no longer available in storage");
        }

        Resource resource = new FileSystemResource(path);

        // Built through ContentDisposition so a stored file name containing quotes
        // or newlines cannot alter the response header.
        ContentDisposition disposition =
                (asAttachment ? ContentDisposition.attachment() : ContentDisposition.inline())
                        .filename(file.fileName(), StandardCharsets.UTF_8)
                        .build();

        MediaType contentType;

        try {
            contentType = MediaType.parseMediaType(file.fileType());
        } catch (Exception e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        long contentLength;

        try {
            contentLength = resource.contentLength();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.GONE, "This file is no longer available in storage", e);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(contentType)
                .contentLength(contentLength)
                .body(resource);
    }
}
