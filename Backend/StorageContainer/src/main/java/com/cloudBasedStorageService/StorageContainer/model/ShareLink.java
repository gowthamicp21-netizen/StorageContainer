package com.cloudBasedStorageService.StorageContainer.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * An "anyone with the link" grant.
 *
 * Kept separate from {@link Share} on purpose: a Share names a recipient whose own
 * token is checked against it, while a ShareLink authorises whichever anonymous
 * visitor presents the token. One access-check path cannot serve both, so folding
 * them together would mean re-deriving the difference at every call site.
 */
@Data
@Entity
@Table(
        name = "share_links",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_share_link_token", columnNames = "token"),
                @UniqueConstraint(name = "uk_share_link_item", columnNames = {"item_id", "item_type"})
        }
)
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Random, unguessable and the only credential a visitor needs, so it is the
     * lookup key for every public read.
     */
    @Column(name = "token", nullable = false, length = 32)
    private String token;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
