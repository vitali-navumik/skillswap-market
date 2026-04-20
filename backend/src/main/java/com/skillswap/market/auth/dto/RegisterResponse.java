package com.skillswap.market.auth.dto;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.UserStatus;
import java.util.Set;
import java.util.UUID;

public record RegisterResponse(
        Long id,
        UUID publicId,
        String email,
        Set<Role> roles,
        UserStatus status
) {
}
