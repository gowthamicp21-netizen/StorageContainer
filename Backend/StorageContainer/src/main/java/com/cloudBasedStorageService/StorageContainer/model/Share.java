package com.cloudBasedStorageService.StorageContainer.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "shares")
@Data
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    private Integer itemId;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    private Role permission;

    @ManyToOne
    @JoinColumn(name = "shared_by", nullable = false)
    private User sharedBy;
}
