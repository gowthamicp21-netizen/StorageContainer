package com.cloudBasedStorageService.StorageContainer.service;


import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.FileDownloadResponse;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoredFileService {

    private final FileRepository storedFileRepository;
    private final FolderRepository folderRepository;
    private final SupabaseStorageService supabaseStorageService;

    public File uploadFile(
            MultipartFile file,
            Integer folderId,
            User user
    ) throws Exception {


        Folder folder = null;

        if (folderId != null) {

            folder = folderRepository
                    .findById(folderId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Folder not found"
                            )
                    );

            if (!folder.getCreatedBy()
                    .getUserId()
                    .equals(user.getUserId())) {

                throw new RuntimeException(
                        "You do not have access to this folder"
                );
            }
        }

        String filePath = supabaseStorageService.uploadFile( file, user.getUserId());

        File storedFile = new File();

        storedFile.setFileName(
                file.getOriginalFilename()
        );

        storedFile.setFileType(
                file.getContentType()
        );

        storedFile.setFileSize(
                file.getSize()
        );

        storedFile.setFilePath(filePath);

        storedFile.setFolder(folder);

        storedFile.setCreatedBy(user);

        return storedFileRepository.save(
                storedFile
        );
    }

    public List<File> getFiles(
            Integer folderId,
            User user
    ) {

        if (folderId == null) {

            return storedFileRepository
                    .findByCreatedByAndFolderIsNull(user);
        }

        Folder folder = folderRepository
                .findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Folder not found"
                        )
                );

        if (!folder.getCreatedBy()
                .getUserId()
                .equals(user.getUserId())) {

            throw new RuntimeException(
                    "You do not have access to this folder"
            );
        }

        return storedFileRepository
                .findByCreatedByAndFolder(
                        user,
                        folder
                );
    }

    public FileDownloadResponse downloadFile(int fileId, User user) {
        File file=storedFileRepository.findById(fileId).get();
        if(file==null){
            throw new RuntimeException(
                    "You do not have file"
            );
        }
        if(file.getCreatedBy().getUserId()!=user.getUserId()){
            throw new RuntimeException(
                    "You do not have access to this file"
            );
        }
        String filePath=file.getFilePath();
        byte[] fileData=supabaseStorageService.downloadFile(filePath);
        FileDownloadResponse response=new FileDownloadResponse(file.getFileName(),file.getFileType(),fileData);
        return response;
    }

    public void deleteFile(int fileId, User user) {
        File file = storedFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found")
                );
        if(!file.getCreatedBy().getUserId().equals(user.getUserId())){
            throw new RuntimeException(
                    "You do not have access to this file"
            );
        }
        supabaseStorageService.deleteFile(file);
        storedFileRepository.delete(file);

    }

    public File renameFile(
            Integer fileId,
            String newName,
            User user) {

        File file = storedFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        if (!file.getCreatedBy()
                .getUserId()
                .equals(user.getUserId())) {

            throw new RuntimeException(
                    "You do not have access to this file");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException(
                    "File name cannot be empty");
        }

        file.setFileName(newName.trim());

        return storedFileRepository.save(file);
    }
}