package com.cloudBasedStorageService.StorageContainer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(
            MultipartFile file,
            Integer userId
    ) throws IOException {

        Path userDirectory = Paths.get(
                uploadDir,
                String.valueOf(userId)
        );

        Files.createDirectories(userDirectory);

        String originalFileName = file.getOriginalFilename();

        String extension = "";

        if (originalFileName != null &&
                originalFileName.contains(".")) {

            extension = originalFileName.substring(
                    originalFileName.lastIndexOf(".")
            );
        }

        String storedFileName =
                UUID.randomUUID() + extension;


        Path filePath =
                userDirectory.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return filePath.toString();
    }
}
