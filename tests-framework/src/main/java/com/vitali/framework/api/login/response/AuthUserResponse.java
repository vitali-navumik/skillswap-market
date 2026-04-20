package com.vitali.framework.api.login.response;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class AuthUserResponse {
    private Long id;
    private UUID publicId;
    private UUID walletPublicId;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private Set<UserRole> roles;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
