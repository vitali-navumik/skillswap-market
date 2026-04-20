package com.vitali.framework.api.register.responses;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RegisterUserResponse {
    private Long id;
    private UUID publicId;
    private String email;
    private Set<UserRole> roles;
    private UserStatus status;
}
