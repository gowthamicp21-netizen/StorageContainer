package com.cloudBasedStorageService.StorageContainer.model.dto;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Deliberately has no {@code id} or {@code sharedBy}: clients must not be able to
 * target an existing row for overwrite or claim another user as the sharer.
 */
public record ShareRequest(
        @NotBlank @Email String email,
        @NotNull Integer itemId,
        @NotNull ItemType itemType,
        @NotNull Role permission
) {
}
