package com.cloudBasedStorageService.StorageContainer.repo;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder,Integer> {

    @Query("SELECT f FROM Folder f WHERE f.parentFolder IS NULL")
    List<Folder> findByCreatedBy(User user);

    List<Folder> findByParentFolder(Folder parentFolder);

    List<Folder> findByParentFolderId(Integer id);
}
