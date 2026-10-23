package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.dto.ApiErrorResponse;
import com.cloudBasedStorageService.StorageContainer.model.dto.AuthResponse;
import com.cloudBasedStorageService.StorageContainer.model.dto.ForgotPasswordRequest;
import com.cloudBasedStorageService.StorageContainer.model.dto.RegisterRequest;
import com.cloudBasedStorageService.StorageContainer.model.dto.ResetPasswordRequest;
import com.cloudBasedStorageService.StorageContainer.model.dto.UserDto;
import com.cloudBasedStorageService.StorageContainer.model.dto.UserLoginInfo;
import com.cloudBasedStorageService.StorageContainer.service.JwtService;
import com.cloudBasedStorageService.StorageContainer.service.PasswordResetService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            PasswordResetService passwordResetService) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegisterRequest request) {

        return UserDto.from(userService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody UserLoginInfo request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.userEmail(),
                        request.userPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return new AuthResponse(jwtService.generateToken(userDetails));
    }

    @GetMapping("/me")
    public UserDto getUserDetails() {

        String userEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return UserDto.from(userService.getUserByUsername(userEmail));
    }

    @PostMapping("/forgot-password")
    public ApiErrorResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestReset(request.userEmail());

        return new ApiErrorResponse(
                "If that address has an account, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ApiErrorResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request.token(), request.newPassword());

        return new ApiErrorResponse("Your password has been updated. Sign in to continue.");
    }
}
