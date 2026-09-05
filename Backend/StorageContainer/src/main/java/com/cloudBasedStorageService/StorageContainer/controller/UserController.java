package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.UserLoginInfo;
import com.cloudBasedStorageService.StorageContainer.service.JwtService;
import com.cloudBasedStorageService.StorageContainer.service.MyUserDetailService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyUserDetailService userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        userService.register(user);
        return new ResponseEntity<>("Registered Successfully",HttpStatus.OK);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserLoginInfo user){

        System.out.println("LOGIN EMAIL: " + user.userEmail());

        Authentication authentication=authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(user.userEmail(),user.userPassword()));
        if(authentication.isAuthenticated()){
            UserDetails userDetails =
                    (UserDetails) authentication.getPrincipal();

            return jwtService.generateToken(userDetails);
        }
        return "Failure";
    }

    @GetMapping("/me")
    public ResponseEntity<User> getUserDetails(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.getUserByUsername(userEmail);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody UserLoginInfo user){
        System.out.println("Forgot password"+user.userEmail());
        userService.forgotPassword(user);
        return new ResponseEntity<>("Password reset successful",HttpStatus.OK);
    }
}
