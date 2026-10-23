package com.cloudBasedStorageService.StorageContainer.service;


import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.FileDownloadResponse;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.ShareRepository;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoredFileService {

    private final FileRepository storedFileRepository;
    private final FolderRepository folderRepository;
    private final FileStorageService fileStorageService;
    private final ShareRepository shareRepository;
    private final ShareService shareService;
    private final ShareLinkService shareLinkService;

    public File uploadFile(
            MultipartFile file,
            Integer folderId,
            User user
    ) {


        Folder folder = null;

        if (folderId != null) {

            folder = folderRepository
                    .findById(folderId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Folder not found"
                            )
                    );

            if (!folder.getCreatedBy()
                    .getUserId()
                    .equals(user.getUserId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You do not have access to this folder");
            }
        }

        String filePath = fileStorageService.store(file, user.getUserId());

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

        try {
            return storedFileRepository.save(storedFile);
        } catch (RuntimeException e) {
            // No row means nothing can ever point at the blob, so it would leak.
            fileStorageService.delete(filePath);
            throw e;
        }
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
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Folder not found")
                );

        if (!folder.getCreatedBy()
                .getUserId()
                .equals(user.getUserId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have access to this folder"
            );
        }

        return storedFileRepository
                .findByCreatedByAndFolder(
                        user,
                        folder
                );
    }

    public FileDownloadResponse downloadFile(int fileId, User user) {

        File file = requireReadableFile(fileId, user);

        return new FileDownloadResponse(
                file.getFileName(),
                file.getFileType(),
                fileStorageService.resolve(file.getFilePath())
        );
    }

    public void deleteFile(int fileId, User user) {
        File file = storedFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
                );
        if(!file.getCreatedBy().getUserId().equals(user.getUserId())){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have access to this file");
        }
        storedFileRepository.delete(file);
        fileStorageService.delete(file.getFilePath());

        // A link outlives nothing: without this the revoked-by-deletion file would
        // still be advertised by the share row, and an id reuse would expose content.
        shareLinkService.revokeForItem(ItemType.FILE, file.getId());

    }

    public File renameFile(
            Integer fileId,
            String newName,
            User user) {

        File file = storedFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        if (!file.getCreatedBy()
                .getUserId()
                .equals(user.getUserId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have access to this file");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File name cannot be empty");
        }

        file.setFileName(newName.trim());

        return storedFileRepository.save(file);
    }

    public FileDownloadResponse viewFile(int fileId, User user) {

        File file = requireReadableFile(fileId, user);

        return new FileDownloadResponse(
                file.getFileName(),
                file.getFileType(),
                fileStorageService.resolve(file.getFilePath())
        );
    }

    /**
     * Download and view previously disagreed on whether a share inherited from an
     * ancestor folder counted, so both now go through this one check.
     */
    private File requireReadableFile(int fileId, User user) {

        File file = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File not found"));

        if (file.getCreatedBy().getUserId().equals(user.getUserId())) {
            return file;
        }

        String email = Emails.normalize(user.getUserEmail());

        boolean sharedDirectly = shareRepository
                .findByEmailAndItemIdAndItemType(email, fileId, ItemType.FILE)
                .isPresent();

        boolean sharedViaFolder = file.getFolder() != null
                && shareService.hasFolderAccess(file.getFolder().getId(), email);

        if (!sharedDirectly && !sharedViaFolder) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have access to this file");
        }

        return file;
    }
}