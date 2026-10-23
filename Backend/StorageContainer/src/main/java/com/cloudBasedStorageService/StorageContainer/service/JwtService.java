package com.cloudBasedStorageService.StorageContainer.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final Duration tokenLifetime;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiry-minutes:30}") long expiryMinutes) {

        byte[] keyBytes;

        try {
            // jjwt raises DecodingException rather than IllegalArgumentException
            // for malformed input, so both are reported the same way.
            keyBytes = Decoders.BASE64.decode(base64Secret);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "app.jwt.secret must be base64-encoded. "
                            + "Generate one with: openssl rand -base64 32",
                    e);
        }

        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must decode to at least 256 bits. "
                            + "Generate one with: openssl rand -base64 32");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.tokenLifetime = Duration.ofMinutes(expiryMinutes);
    }

    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("roles",
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());

        Instant issuedAt = Instant.now();

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(tokenLifetime)))
                .signWith(signingKey)
                .compact();
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {

        try {
            Claims claims = extractAllClaims(token);

            String userEmail = claims.getSubject();

            return userEmail != null
                    && userEmail.equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
