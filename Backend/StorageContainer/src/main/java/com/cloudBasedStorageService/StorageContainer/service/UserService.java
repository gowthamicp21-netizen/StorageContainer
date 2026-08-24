package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.UserLoginInfo;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;


    public void register(User user) {
        userRepo.save(user);
    }

    public void forgotPassword(UserLoginInfo user) {
        Optional<User> usr=userRepo.findByUserEmail(user.userEmail());
        usr.get().setUserPassword(user.userPassword());
        userRepo.save(usr.get());
    }

    public User getUserByUsername(String userEmail) {
        Optional<User> user=userRepo.findByUserEmail(userEmail);
        return user.get();
    }
}
