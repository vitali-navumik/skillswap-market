package users;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.users.invocations.CreateUserByAdminInvocation;
import com.vitali.framework.api.users.invocations.CreateUserByAdminInvocation.CreateUserTestCase;
import com.vitali.framework.api.users.providers.UserRoleProvider.RoleNotAllowedToManageUsers;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.api.users.responses.CreateUserResponse;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.resolvers.UserCreationHelper;
import com.vitali.framework.tags.UserTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UserTag
@ExtendWith({GlobalActionsParameterResolver.class})
class UserCreationTests {

    @TestTemplate
    @ExtendWith(CreateUserByAdminInvocation.class)
    void adminCanCreateUser(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                            CreateUserTestCase testCase) {
        CreateUserRequest request = testCase.buildRequest();
        UUID publicId = admin.usersActions().createUserResponse(request).getPublicId();
        GetUserResponse createdUser = admin.usersActions().getUserResponse(publicId);
        testCase.assertResult(createdUser, request);
    }

    @Test
    void adminCannotCreateUserWithDuplicateEmail(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                                                 @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        CreateUserRequest request = CreateUserRequest.builder()
                .email(student.userInfo().getEmail())
                .roles(Set.of(UserRole.STUDENT))
                .build();

        ConnectorResponse<CreateUserResponse> response = admin.usersActions().createUser(request);

        CommonAssertions.checkConflict(response);
        assertThat(response.getDataResponse()).contains("Email is already in use");
    }

    @ParameterizedTest
    @EnumSource(RoleNotAllowedToManageUsers.class)
    void userWithoutAdminRoleCannotCreateUser(RoleNotAllowedToManageUsers role) {
        ActionsContainer actor = resolveUserActions(role);
        CreateUserRequest request = CreateUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .build();

        ConnectorResponse<CreateUserResponse> response = actor.usersActions().createUser(request);

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse()).contains("Access denied");
    }

    private ActionsContainer resolveUserActions(RoleNotAllowedToManageUsers role) {
        return switch (role) {
            case STUDENT, MENTOR -> UserCreationHelper.createUserAndLogIn(UserPreset.valueOf(role.name()));
        };
    }
}
