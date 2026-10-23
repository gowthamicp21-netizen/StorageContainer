package com.cloudBasedStorageService.StorageContainer.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String userEmail;

    /**
     * Only a hash of the delivered token is stored, so a database leak does not
     * hand over working reset links.
     */
    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant consumedAt;
}
