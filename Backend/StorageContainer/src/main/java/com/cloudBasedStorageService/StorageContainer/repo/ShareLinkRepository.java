package com.cloudBasedStorageService.StorageContainer.repo;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Integer> {

    Optional<ShareLink> findByToken(String token);

    Optional<ShareLink> findByItemIdAndItemType(Integer itemId, ItemType itemType);
}
