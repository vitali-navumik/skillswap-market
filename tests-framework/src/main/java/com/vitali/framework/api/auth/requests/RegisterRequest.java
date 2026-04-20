package com.vitali.framework.api.auth.requests;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.utils.FakerGenerator;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RegisterRequest {
    @Builder.Default
    private String email = FakerGenerator.randomEmail();
    @Builder.Default
    private String password = FakerGenerator.randomPassword();
    @Builder.Default
    private String firstName = FakerGenerator.randomFirstName();
    @Builder.Default
    private String lastName = FakerGenerator.randomLastName();
    private Set<UserRole> roles;
}
