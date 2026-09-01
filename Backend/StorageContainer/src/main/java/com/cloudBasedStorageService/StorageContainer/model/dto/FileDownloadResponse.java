package com.cloudBasedStorageService.StorageContainer.model.dto;

public record FileDownloadResponse (String fileName,String fileType, byte[] fileData){

}
