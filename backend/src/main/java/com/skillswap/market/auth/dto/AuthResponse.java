package com.skillswap.market.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthUserResponse user
) {
}
