package com.vitali.framework.api.auth.responses;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RegisterResponse {
    private Long id;
    private UUID publicId;
    private String email;
    private Set<UserRole> roles;
    private UserStatus status;
}
