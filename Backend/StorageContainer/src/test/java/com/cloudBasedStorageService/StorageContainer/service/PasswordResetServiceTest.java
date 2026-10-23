package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.PasswordResetToken;
import com.cloudBasedStorageService.StorageContainer.model.Role;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.PasswordResetTokenRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private static final String EMAIL = "someone@example.com";

    private PasswordResetTokenRepository tokenRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ResetEmailService emailService;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {

        tokenRepository = mock(PasswordResetTokenRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(ResetEmailService.class);

        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service = new PasswordResetService(
                tokenRepository,
                userRepository,
                passwordEncoder,
                emailService,
                15,
                "http://localhost:5173/reset-password");
    }

    private User existingUser() {

        User user = new User();

        user.setUserId(7);
        user.setUserEmail(EMAIL);
        user.setUserRole(Role.PUBLIC_USER);
        user.setUserPassword("old-hash");

        return user;
    }

    @Test
    void sendsNothingForAnUnknownAddressButDoesNotRevealIt() {

        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.empty());

        service.requestReset(EMAIL);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendResetLink(anyString(), anyString());
    }

    @Test
    void storesOnlyAHashOfTheTokenAndSendsTheRawValueOnce() {

        when(userRepository.findByUserEmail(EMAIL))
                .thenReturn(Optional.of(existingUser()));

        service.requestReset(EMAIL);

        PasswordResetToken saved = capturedSavedToken();

        assertThat(saved.getTokenHash()).hasSize(64);
        assertThat(saved.getConsumedAt()).isNull();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendResetLink(eq(EMAIL), link.capture());

        assertThat(link.getValue())
                .startsWith("http://localhost:5173/reset-password?token=")
                .doesNotContain(saved.getTokenHash());
    }

    @Test
    void invalidatesPreviouslyIssuedLinks() {

        when(userRepository.findByUserEmail(EMAIL))
                .thenReturn(Optional.of(existingUser()));

        PasswordResetToken outstanding = new PasswordResetToken();

        outstanding.setUserEmail(EMAIL);
        outstanding.setTokenHash("previous-hash");
        outstanding.setExpiresAt(Instant.now().plusSeconds(600));

        when(tokenRepository.findByUserEmailAndConsumedAtIsNull(EMAIL))
                .thenReturn(List.of(outstanding));

        service.requestReset(EMAIL);

        assertThat(outstanding.getConsumedAt()).isNotNull();
    }

    @Test
    void blocksMoreThanThreeRequestsInAWindow() {

        when(userRepository.findByUserEmail(EMAIL))
                .thenReturn(Optional.of(existingUser()));

        when(tokenRepository.countByUserEmailAndCreatedAtAfter(eq(EMAIL), any()))
                .thenReturn(3L);

        assertThatThrownBy(() -> service.requestReset(EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        verify(emailService, never()).sendResetLink(anyString(), anyString());
    }

    @Test
    void resetsThePasswordAndConsumesTheToken() {

        User user = existingUser();

        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(user));

        service.requestReset(EMAIL);

        PasswordResetToken saved = capturedSavedToken();

        when(tokenRepository.findByTokenHash(saved.getTokenHash()))
                .thenReturn(Optional.of(saved));

        when(passwordEncoder.encode("brand-new-password")).thenReturn("new-hash");

        service.resetPassword(rawTokenFrom(saved), "brand-new-password");

        assertThat(user.getUserPassword()).isEqualTo("new-hash");
        assertThat(saved.getConsumedAt()).isNotNull();

        verify(userRepository).save(user);
        verify(tokenRepository, times(2)).save(any(PasswordResetToken.class));
    }

    @Test
    void rejectsAnExpiredTokenWithoutChangingThePassword() {

        PasswordResetToken expired = new PasswordResetToken();

        expired.setUserEmail(EMAIL);
        expired.setTokenHash("hash");
        expired.setExpiresAt(Instant.now().minusSeconds(60));

        when(tokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() ->
                service.resetPassword("whatever", "brand-new-password"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("expired");

        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsAnAlreadyConsumedToken() {

        PasswordResetToken used = new PasswordResetToken();

        used.setUserEmail(EMAIL);
        used.setTokenHash("hash");
        used.setExpiresAt(Instant.now().plusSeconds(600));
        used.setConsumedAt(Instant.now().minusSeconds(5));

        when(tokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(used));

        assertThatThrownBy(() ->
                service.resetPassword("whatever", "brand-new-password"))
                .isInstanceOf(ResponseStatusException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsATokenThatIsNotInStorage() {

        when(tokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.resetPassword("guessed", "brand-new-password"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not valid");
    }

    private PasswordResetToken capturedSavedToken() {

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(tokenRepository).save(captor.capture());

        return captor.getValue();
    }

    private String rawTokenFrom(PasswordResetToken saved) {

        // The raw token is not recoverable from its hash, so the test reaches for
        // the value the service handed to the email link.
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendResetLink(eq(EMAIL), link.capture());

        return link.getValue().substring(link.getValue().indexOf("token=") + 6);
    }
}
