package com.vitali.framework.api.users.responses;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public abstract class BaseUserResponse {
    Long id;
    UUID publicId;
    UUID walletPublicId;
    String email;
    String firstName;
    String lastName;
    String displayName;
    Set<UserRole> roles;
    UserStatus status;
    Instant createdAt;
    Instant updatedAt;
}
