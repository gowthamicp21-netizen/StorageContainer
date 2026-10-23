package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.PasswordResetToken;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.PasswordResetTokenRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetEmailService resetEmailService;
    private final Duration tokenLifetime;
    private final String resetLinkBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ResetEmailService resetEmailService,
            @Value("${app.auth.reset-token-ttl-minutes}") long tokenTtlMinutes,
            @Value("${app.auth.reset-link-base-url}") String resetLinkBaseUrl) {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetEmailService = resetEmailService;
        this.tokenLifetime = Duration.ofMinutes(tokenTtlMinutes);
        this.resetLinkBaseUrl = resetLinkBaseUrl;
    }

    /**
     * Always completes the same way whether or not the address is registered, so
     * the endpoint cannot be used to confirm which emails have accounts.
     */
    @Transactional
    public void requestReset(String rawEmail) {

        String email = Emails.normalize(rawEmail);

        Optional<User> user = userRepository.findByUserEmail(email);

        if (user.isEmpty()) {
            return;
        }

        Instant windowStart = Instant.now().minus(tokenLifetime);

        if (tokenRepository.countByUserEmailAndCreatedAtAfter(email, windowStart)
                >= MAX_REQUESTS_PER_WINDOW) {

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many reset requests. Please try again later.");
        }

        List<PasswordResetToken> outstanding =
                tokenRepository.findByUserEmailAndConsumedAtIsNull(email);

        // A new request invalidates every previously issued link.
        outstanding.forEach(token -> token.setConsumedAt(Instant.now()));
        tokenRepository.saveAll(outstanding);

        String tokenValue = newTokenValue();

        PasswordResetToken token = new PasswordResetToken();

        token.setUserEmail(email);
        token.setTokenHash(hashToken(tokenValue));
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(tokenLifetime));

        tokenRepository.save(token);

        // Throwing here rolls the transaction back, so no token survives an
        // email that was never delivered.
        resetEmailService.sendResetLink(
                email,
                resetLinkBaseUrl + "?token=" + tokenValue);
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {

        PasswordResetToken token = tokenRepository
                .findByTokenHash(hashToken(tokenValue))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "This reset link is not valid."));

        if (token.getConsumedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This reset link has expired. Please request a new one.");
        }

        User user = userRepository.findByUserEmail(token.getUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "This reset link is not valid."));

        user.setUserPassword(passwordEncoder.encode(newPassword));

        token.setConsumedAt(Instant.now());

        userRepository.save(user);
        tokenRepository.save(token);
    }

    private String newTokenValue() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String tokenValue) {

        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(tokenValue.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required for reset tokens", e);
        }
    }
}
