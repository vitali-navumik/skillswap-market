package com.skillswap.market.user.dto;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateUserRequest(
        @Email @NotBlank @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotEmpty Set<Role> roles,
        UserStatus status
) {
}
