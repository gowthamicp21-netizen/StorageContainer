package com.cloudBasedStorageService.StorageContainer.repo;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File,Integer> {


    List<File> findByCreatedByAndFolder(
            User user,
            Folder folder
    );

    List<File> findByCreatedByAndFolderIsNull(
            User user
    );

    List<File> findByFolderId(Integer id);

    List<File> findByFolder(Folder folder);
}
