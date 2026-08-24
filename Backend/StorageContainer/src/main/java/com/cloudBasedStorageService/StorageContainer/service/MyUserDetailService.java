package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
       Optional<User> user=userRepo.findByUserEmail(userEmail);

        if(user.isEmpty()){
            System.out.println("User not found");
            throw new UsernameNotFoundException(userEmail);
        }
        return org.springframework.security.core.userdetails.User.
                withUsername(user.get().userEmail)
                .password(user.get().userPassword)
                .authorities("ROLE_"+user.get().userRole)
                .build();



    }
}
