package com.cloudBasedStorageService.StorageContainer.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "shares",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_share_recipient_item",
                columnNames = {"email", "itemId", "itemType"}
        )
)
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Integer itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role permission;

    @ManyToOne
    @JoinColumn(name = "shared_by", nullable = false)
    private User sharedBy;
}
