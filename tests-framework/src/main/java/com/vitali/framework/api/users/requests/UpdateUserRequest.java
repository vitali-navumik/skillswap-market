package com.vitali.framework.api.users.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class UpdateUserRequest {
    @JsonIgnore
    private UUID publicId;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String displayName;
    private Set<UserRole> roles;
    private UserStatus status;
}
