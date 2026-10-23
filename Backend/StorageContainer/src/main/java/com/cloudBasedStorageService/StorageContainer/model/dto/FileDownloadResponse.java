package com.cloudBasedStorageService.StorageContainer.model.dto;

import java.nio.file.Path;

/**
 * Carries a path rather than the file's bytes so the controller can stream it.
 */
public record FileDownloadResponse(String fileName, String fileType, Path path) {
}
