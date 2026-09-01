package com.cloudBasedStorageService.StorageContainer.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true,nullable = false)
    private String userEmail;

    @Column(nullable = true)
    private String userPassword;

    @Enumerated(EnumType.STRING)
    private Role userRole;


}
