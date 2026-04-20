package users;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.users.assertions.UserAssertions;
import com.vitali.framework.api.users.helpers.UsersHelper;
import com.vitali.framework.api.users.invocations.CreateUserByAdminInvocation;
import com.vitali.framework.api.users.invocations.CreateUserByAdminInvocation.CreateUserTestCase;
import com.vitali.framework.api.users.invocations.UpdateUserByAdminInvocation;
import com.vitali.framework.api.users.invocations.UpdateUserByAdminInvocation.UpdateUserTestCase;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.api.users.requests.UpdateUserRequest;
import com.vitali.framework.api.users.responses.CreateUserResponse;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.resolvers.UserCreationHelper;
import com.vitali.framework.tags.UserTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import com.vitali.framework.resolvers.GlobalActionsParameterResolver;

@UserTag
@ExtendWith({GlobalActionsParameterResolver.class})
class UsersTests {

    @Test
    void adminCanGetUsersList(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                              @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        List<GetUserResponse> users = admin.usersActions().getUsersResponse();
        GetUserResponse createdStudent = UsersHelper.getUserByPublicId(users, student.userInfo().getPublicId());

        UserAssertions.checkUserDataIsCorrect(createdStudent, new UserAssertions.AssertionParams()
                .email(student.userInfo().getEmail())
                .firstName(student.userInfo().getFirstName())
                .lastName(student.userInfo().getLastName())
                .roles(List.of(UserRole.STUDENT))
                .status(UserStatus.ACTIVE.name()));
    }

    @Test
    void adminCanGetUserByPublicId(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                                   @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        GetUserResponse user = admin.usersActions().getUserResponse(student.userInfo().getPublicId());

        UserAssertions.checkUserDataIsCorrect(user, new UserAssertions.AssertionParams()
                .email(student.userInfo().getEmail())
                .firstName(student.userInfo().getFirstName())
                .lastName(student.userInfo().getLastName())
                .roles(List.of(UserRole.STUDENT))
                .status(UserStatus.ACTIVE.name()));
    }

    @TestTemplate
    @ExtendWith(CreateUserByAdminInvocation.class)
    void adminCanCreateUser(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                            CreateUserTestCase testCase) {
        CreateUserRequest request = testCase.buildRequest();
        UUID publicId = admin.usersActions().createUserResponse(request).getPublicId();
        GetUserResponse createdUser = admin.usersActions().getUserResponse(publicId);
        testCase.assertResult(createdUser, request);
    }

    @TestTemplate
    @ExtendWith(UpdateUserByAdminInvocation.class)
    void adminCanUpdateUser(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                            @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student,
                            UpdateUserTestCase testCase) {
        UpdateUserRequest request = testCase.buildRequest(student.userInfo());
        CommonAssertions.checkResponseIsOk(admin.usersActions().updateUser(request));
        GetUserResponse updatedUser = admin.usersActions().getUserResponse(student.userInfo().getPublicId());
        testCase.assertResult(updatedUser, request);
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

    @ParameterizedTest
    @EnumSource(RoleNotAllowedToManageUsers.class)
    void userWithoutAdminRoleCannotGetUsersList(RoleNotAllowedToManageUsers role) {
        ActionsContainer actor = resolveUserActions(role);

        ConnectorResponse<List<GetUserResponse>> response = actor.usersActions().getUsers();

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse()).contains("Access denied");
    }

    private ActionsContainer resolveUserActions(RoleNotAllowedToManageUsers role) {
        return switch (role) {
            case STUDENT, MENTOR -> UserCreationHelper.createUserAndLogIn(UserPreset.valueOf(role.name()));
        };
    }

    enum RoleNotAllowedToManageUsers {
        STUDENT,
        MENTOR
    }
}
