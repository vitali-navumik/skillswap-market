package users;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.users.assertions.UserAssertions;
import com.vitali.framework.api.users.helpers.UsersHelper;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.api.users.providers.UserRoleProvider.RoleNotAllowedToManageUsers;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.resolvers.UserCreationHelper;
import com.vitali.framework.tags.UserTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UserTag
@ExtendWith({GlobalActionsParameterResolver.class})
class UserAccessTests {

    @Test
    @DisplayName("Admin can get users list")
    void adminCanGetUsersList(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                              @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        List<GetUserResponse> users = admin.usersActions().getUsersResponse();
        GetUserResponse createdStudent = UsersHelper.getUserByPublicId(users, student.userInfo().getPublicId());

        UserAssertions.checkUserDataIsCorrect(createdStudent, new UserAssertions.AssertionParams()
                .email(student.userInfo().getEmail())
                .firstName(student.userInfo().getFirstName())
                .lastName(student.userInfo().getLastName())
                .roles(Set.of(UserRole.STUDENT))
                .status(UserStatus.ACTIVE.name()));
    }

    @Test
    @DisplayName("Admin can get user by public id")
    void adminCanGetUserByPublicId(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                                   @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        GetUserResponse user = admin.usersActions().getUserResponse(student.userInfo().getPublicId());

        UserAssertions.checkUserDataIsCorrect(user, new UserAssertions.AssertionParams()
                .email(student.userInfo().getEmail())
                .firstName(student.userInfo().getFirstName())
                .lastName(student.userInfo().getLastName())
                .roles(Set.of(UserRole.STUDENT))
                .status(UserStatus.ACTIVE.name()));
    }

    @ParameterizedTest
    @EnumSource(RoleNotAllowedToManageUsers.class)
    @DisplayName("User without admin role cannot get users list")
    void userWithoutAdminRoleCannotGetUsersList(RoleNotAllowedToManageUsers role) {
        ActionsContainer actor = resolveUserActions(role);

        ConnectorResponse<List<GetUserResponse>> response = actor.usersActions().getUsers();

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse())
                .as("Only admin can access users list")
                .contains("Access denied");
    }

    @ParameterizedTest
    @EnumSource(RoleNotAllowedToManageUsers.class)
    @DisplayName("User without admin role cannot get other user by public id")
    void userWithoutAdminRoleCannotGetUserByPublicId(RoleNotAllowedToManageUsers role,
                                                     @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        ActionsContainer actor = resolveUserActions(role);

        ConnectorResponse<GetUserResponse> response = actor.usersActions().getUser(student.userInfo().getPublicId());

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse())
                .as("Only admin can access other users' profiles")
                .contains("You can access only your own profile");
    }

    @Test
    @DisplayName("Admin can see inactive user in users list")
    void adminCanSeeInactiveUserInUsersList(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .roles(Set.of(UserRole.STUDENT))
                .status(UserStatus.INACTIVE)
                .build();

        UUID publicId = admin.usersActions().createUserResponse(userRequest).getPublicId();

        List<GetUserResponse> users = admin.usersActions().getUsersResponse();
        assertThat(users)
                .as("Inactive user should be present in users list")
                .extracting(GetUserResponse::getPublicId)
                .contains(publicId);
    }

    private ActionsContainer resolveUserActions(RoleNotAllowedToManageUsers role) {
        return switch (role) {
            case STUDENT, MENTOR -> UserCreationHelper.createUserAndLogIn(UserPreset.valueOf(role.name()));
        };
    }
}
