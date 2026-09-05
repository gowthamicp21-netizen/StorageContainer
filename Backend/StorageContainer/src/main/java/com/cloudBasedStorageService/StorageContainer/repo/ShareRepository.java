package com.cloudBasedStorageService.StorageContainer.repo;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareRepository extends JpaRepository<Share,Integer> {
    List<Share> findAllByEmail(String userEmail);

    Optional<Share> findByEmailAndItemIdAndItemType(String userEmail, Integer itemId, ItemType itemType);


}
