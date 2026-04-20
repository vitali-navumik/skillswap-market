package users;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.users.helpers.UserMapper;
import com.vitali.framework.api.users.invocations.UpdateUserByAdminInvocation;
import com.vitali.framework.api.users.invocations.UpdateUserByAdminInvocation.UpdateUserTestCase;
import com.vitali.framework.api.users.requests.UpdateUserRequest;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.api.users.responses.UpdateUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.tags.UserTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@UserTag
@ExtendWith({GlobalActionsParameterResolver.class})
class UserEditTests {

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
    void adminCannotUpdateUserWithDuplicateEmail(@GlobalActionsPreset(UserPreset.ADMIN) ActionsContainer admin,
                                                 @GlobalActionsPreset(UserPreset.STUDENT) ActionsContainer student) {
        UpdateUserRequest request = UserMapper.INSTANCE.toUpdateUserRequest(student.userInfo())
                .toBuilder()
                .email(admin.userInfo().getEmail())
                .build();

        ConnectorResponse<UpdateUserResponse> response = admin.usersActions().updateUser(request);

        CommonAssertions.checkConflict(response);
        assertThat(response.getDataResponse())
                .as("Duplicated email is not allowed")
                .contains("Email is already in use");
    }
}
