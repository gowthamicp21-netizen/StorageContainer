package com.cloudBasedStorageService.StorageContainer.model.dto;

import java.util.List;

/**
 * Everything an anonymous visitor may see about a linked item.
 *
 * Deliberately omits storage paths and owner ids: {@code filePath} is the on-disk
 * key, and exposing it would let a leak of the database be used to read blobs
 * directly.
 */
public record PublicShareView(
        String itemType,
        Integer itemId,
        String itemName,
        String ownerEmail,
        List<Item> folders,
        List<Item> files
) {

    public record Item(
            Integer id,
            String name,
            String mimeType,
            Long sizeInBytes
    ) {
    }
}
