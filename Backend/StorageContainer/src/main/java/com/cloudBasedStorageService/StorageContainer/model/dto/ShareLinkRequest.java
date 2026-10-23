package com.cloudBasedStorageService.StorageContainer.model.dto;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import jakarta.validation.constraints.NotNull;

/**
 * No id, token or owner fields: the server assigns the token and derives the owner
 * from the caller, so a client cannot target another item or claim someone else's link.
 */
public record ShareLinkRequest(
        @NotNull Integer itemId,
        @NotNull ItemType itemType
) {
}
