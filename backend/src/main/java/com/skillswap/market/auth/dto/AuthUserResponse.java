package com.skillswap.market.auth.dto;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthUserResponse(
        Long id,
        UUID publicId,
        UUID walletPublicId,
        String email,
        String firstName,
        String lastName,
        String displayName,
        Set<Role> roles,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
