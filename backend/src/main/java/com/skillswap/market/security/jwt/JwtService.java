package com.skillswap.market.security.jwt;

import com.skillswap.market.security.config.JwtProperties;
import com.skillswap.market.security.model.AppUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(resolveSecret(jwtProperties.secret()));
    }

    public String generateToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.expirationMs());

        return Jwts.builder()
                .subject(principal.getUsername())
                .claims(Map.of(
                        "userId", principal.id(),
                        "roles", principal.roles()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, AppUserPrincipal principal) {
        String username = extractUsername(token);
        return username.equals(principal.getUsername()) && !isTokenExpired(token);
    }

    public long getExpirationMs() {
        return jwtProperties.expirationMs();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] resolveSecret(String configuredSecret) {
        try {
            return Decoders.BASE64.decode(configuredSecret);
        } catch (RuntimeException ignored) {
            return configuredSecret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
