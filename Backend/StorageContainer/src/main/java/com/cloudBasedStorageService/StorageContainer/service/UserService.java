package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.Role;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.RegisterRequest;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {

        String email = Emails.normalize(request.userEmail());

        if (userRepo.findByUserEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists with that email.");
        }

        User user = new User();

        user.setUserEmail(email);
        user.setUserPassword(passwordEncoder.encode(request.userPassword()));

        // Roles are never taken from the request body.
        user.setUserRole(Role.PUBLIC_USER);

        return userRepo.save(user);
    }

    public User getUserByUsername(String userEmail) {

        return userRepo.findByUserEmail(Emails.normalize(userEmail))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }
}
