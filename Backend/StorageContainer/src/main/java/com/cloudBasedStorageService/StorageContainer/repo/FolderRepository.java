package com.cloudBasedStorageService.StorageContainer.repo;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder,Integer> {


    List<Folder> findByCreatedByAndParentFolderIsNull(User user);

    List<Folder> findByCreatedByAndParentFolder(User user, Folder parentFolder);

    List<Folder> findByParentFolder(Folder parentFolder);
}
