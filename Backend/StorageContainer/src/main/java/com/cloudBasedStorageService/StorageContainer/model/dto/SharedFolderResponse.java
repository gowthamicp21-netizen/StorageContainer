package com.cloudBasedStorageService.StorageContainer.model.dto;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.Folder;

import java.util.List;

public record SharedFolderResponse(
        Folder folder,
        List<Folder> folders,
        List<File> files
) {
}