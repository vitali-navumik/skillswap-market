package com.skillswap.market.user.dto;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
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
