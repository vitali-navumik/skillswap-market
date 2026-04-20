package com.vitali.framework.api.users.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import java.util.UUID;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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
