package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailService implements UserDetailsService {

    private final UserRepository userRepo;

    public MyUserDetailService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail)
            throws UsernameNotFoundException {

        Optional<User> user = userRepo.findByUserEmail(Emails.normalize(userEmail));

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User found = user.get();

        return org.springframework.security.core.userdetails.User
                .withUsername(found.getUserEmail())
                .password(found.getUserPassword() == null ? "" : found.getUserPassword())
                .authorities("ROLE_" + (found.getUserRole() == null
                        ? com.cloudBasedStorageService.StorageContainer.model.Role.PUBLIC_USER.name()
                        : found.getUserRole().name()))
                .build();
    }
}
