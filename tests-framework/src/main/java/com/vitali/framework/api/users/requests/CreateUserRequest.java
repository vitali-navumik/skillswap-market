package com.vitali.framework.api.users.requests;

import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.utils.FakerGenerator;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
    @Builder.Default
    private String email = FakerGenerator.randomEmail();
    @Builder.Default
    private String password = "Password123";
    @Builder.Default
    private String firstName = FakerGenerator.randomFirstName();
    @Builder.Default
    private String lastName = FakerGenerator.randomLastName();
    private Set<UserRole> roles;
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}
