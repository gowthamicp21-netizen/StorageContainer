package com.cloudBasedStorageService.StorageContainer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores file content on the local filesystem. Keys are relative to the configured
 * root, so the upload directory can be moved without rewriting the database.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path root;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {

        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create the upload directory at " + root, e);
        }

        // A relative file.upload-dir resolves against the JVM working directory,
        // which differs between an IDE run and `mvn spring-boot:run`.
        log.info("Uploaded files are stored under {}", root);
    }

    /**
     * @return the relative key to persist, or an error if the file could not be stored.
     */
    public String store(MultipartFile file, Integer userId) {

        String extension = extensionOf(file.getOriginalFilename());

        String key = userId + "/" + UUID.randomUUID() + extension;

        Path target = resolve(key);

        try {

            Files.createDirectories(target.getParent());

            // Copied from the stream rather than getBytes() so a large upload does
            // not have to fit in the heap.
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the uploaded file", e);
        }

        return key;
    }

    public Path resolve(String key) {

        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "This file has no stored content");
        }

        Path candidate = root.resolve(key).normalize();

        // A persisted key that walks outside the root is a corrupted or tampered row,
        // not a request to be honoured.
        if (!candidate.startsWith(root)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Stored file path is not valid");
        }

        return candidate;
    }

    public void delete(String key) {

        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            // The database row is already gone, so a leftover file must not fail the
            // delete the user asked for.
            log.warn("Could not delete stored file {}", key, e);
        }
    }

    private static String extensionOf(String originalFilename) {

        if (originalFilename == null) {
            return "";
        }

        int dot = originalFilename.lastIndexOf('.');

        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }

        String extension = originalFilename.substring(dot);

        // Client-supplied, so it only reaches the key when it is a plain extension.
        // ".../../../x" would otherwise escape the per-user directory.
        if (extension.length() > 12 || extension.matches("\\.[A-Za-z0-9]+")) {
            return extension;
        }

        return "";
    }
}
