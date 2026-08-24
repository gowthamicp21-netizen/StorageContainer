package com.cloudBasedStorageService.StorageContainer.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer userId;

    @Column(unique = true,nullable = false)
    public String userEmail;

    @Column(nullable = false)
    public String userPassword;

    @Enumerated(EnumType.STRING)
    public Role userRole;


}
