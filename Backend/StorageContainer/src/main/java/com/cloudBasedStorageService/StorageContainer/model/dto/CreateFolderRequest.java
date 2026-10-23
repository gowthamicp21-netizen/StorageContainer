package com.cloudBasedStorageService.StorageContainer.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * No {@code id} and no {@code createdBy}: the owner always comes from the
 * authenticated user, and a client-supplied id would let requests overwrite
 * existing rows.
 */
public record CreateFolderRequest(
        @NotBlank
        @Size(max = 255, message = "Folder name is too long")
        String folderName,

        Integer parentFolderId
) {
}
