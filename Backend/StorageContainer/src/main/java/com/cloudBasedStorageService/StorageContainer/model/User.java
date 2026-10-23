package com.cloudBasedStorageService.StorageContainer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true,nullable = false)
    private String userEmail;

    // File and folder responses embed the createdBy relation, so the hash must
    // never be part of JSON in either direction.
    @JsonIgnore
    @Column(nullable = true)
    private String userPassword;

    @Enumerated(EnumType.STRING)
    private Role userRole;


}
