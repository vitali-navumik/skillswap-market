package com.vitali.framework.utils;

import com.vitali.framework.config.Config;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.UtilityClass;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class JwtTokenGenerator {

    private static final String DEFAULT_TEST_TOKEN_SECRET =
            "skillswap-market-demo-secret-key-that-must-be-long-enough-2026";
    private static final String INVALID_SIGNATURE_TOKEN_SECRET =
            "skillswap-market-invalid-signature-secret-key-2026";

    public static String expiredToken(String email, Long userId, Set<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of(
                        "userId", userId,
                        "roles", roles
                ))
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(signingKey())
                .compact();
    }

    public static String tokenWithInvalidSignature(String email, Long userId, Set<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claims(Map.of(
                        "userId", userId,
                        "roles", roles
                ))
                .issuedAt(Date.from(now.minusSeconds(60)))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(createSigningKey(INVALID_SIGNATURE_TOKEN_SECRET, INVALID_SIGNATURE_TOKEN_SECRET))
                .compact();
    }

    private static SecretKey signingKey() {
        return createSigningKey(Config.TOKEN_SECRET, DEFAULT_TEST_TOKEN_SECRET);
    }

    private static SecretKey createSigningKey(String configuredSecret, String fallbackSecret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(configuredSecret));
        } catch (RuntimeException ignored) {
            byte[] configuredSecretBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
            if (configuredSecretBytes.length >= 32) {
                return Keys.hmacShaKeyFor(configuredSecretBytes);
            }
            return Keys.hmacShaKeyFor(fallbackSecret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
