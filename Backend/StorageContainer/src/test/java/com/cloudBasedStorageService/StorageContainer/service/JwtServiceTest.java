package com.cloudBasedStorageService.StorageContainer.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static String base64Key(int bytes) {

        byte[] raw = new byte[bytes];

        for (int i = 0; i < bytes; i++) {
            raw[i] = (byte) (i + 1);
        }

        return Base64.getEncoder().encodeToString(raw);
    }

    private final JwtService jwtService =
            new JwtService(base64Key(32), 30);

    private final UserDetails user = User
            .withUsername("someone@example.com")
            .password("ignored")
            .authorities("ROLE_PUBLIC_USER")
            .build();

    @Test
    void issuesTokenThatRoundTripsTheEmail() {

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUserEmail(token))
                .isEqualTo("someone@example.com");

        assertThat(jwtService.validateToken(token, user)).isTrue();
    }

    @Test
    void sameSecretKeepsTokensValidAcrossInstances() {

        String token = jwtService.generateToken(user);

        JwtService otherInstance = new JwtService(base64Key(32), 30);

        assertThat(otherInstance.validateToken(token, user)).isTrue();
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {

        String token = new JwtService(base64Key(32), 30).generateToken(user);

        JwtService otherService = new JwtService(base64Key(40), 30);

        assertThat(otherService.validateToken(token, user)).isFalse();
    }

    @Test
    void rejectsTokenIssuedForADifferentSubject() {

        String token = jwtService.generateToken(user);

        UserDetails otherUser = User
                .withUsername("attacker@example.com")
                .password("ignored")
                .authorities("ROLE_PUBLIC_USER")
                .build();

        assertThat(jwtService.validateToken(token, otherUser)).isFalse();
    }

    @Test
    void rejectsExpiredToken() {

        JwtService shortLived = new JwtService(base64Key(32), -1);

        assertThat(shortLived.validateToken(shortLived.generateToken(user), user))
                .isFalse();
    }

    @Test
    void refusesToStartWithASecretThatIsTooShort() {

        assertThatThrownBy(() -> new JwtService(base64Key(16), 30))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    void refusesToStartWithASecretThatIsNotBase64() {

        assertThatThrownBy(() -> new JwtService("not base64 !!", 30))
                .isInstanceOf(IllegalStateException.class);
    }
}
