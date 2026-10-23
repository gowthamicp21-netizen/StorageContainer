package com.cloudBasedStorageService.StorageContainer.repo;

import com.cloudBasedStorageService.StorageContainer.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    List<PasswordResetToken> findByUserEmailAndConsumedAtIsNull(String userEmail);

    long countByUserEmailAndCreatedAtAfter(String userEmail, Instant since);
}
