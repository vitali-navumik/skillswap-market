package com.skillswap.market.user.dto;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdateUserRequest(
        @NotNull UUID publicId,
        @Email @Size(max = 320) String email,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(min = 8, max = 72) String password,
        @Size(max = 150) String displayName,
        Set<Role> roles,
        UserStatus status
) {
}
