package com.vitali.framework.api.users.invocations;

import com.vitali.framework.api.users.assertions.UserAssertions;
import com.vitali.framework.api.users.helpers.UserMapper;
import com.vitali.framework.api.users.helpers.UsersHelper;
import com.vitali.framework.api.users.requests.UpdateUserRequest;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.invocations.GenericAPITestTemplateInvocationContextProvider;
import com.vitali.framework.invocations.TestCaseBase;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class UpdateUserByAdminInvocation implements GenericAPITestTemplateInvocationContextProvider<UpdateUserByAdminInvocation.UpdateUserTestCase> {

    @Override
    public Stream<UpdateUserTestCase> getTestCasesStream() {
        return Stream.of(
                createCase("Update user first name", UserField.FIRST_NAME),
                createCase("Update user last name", UserField.LAST_NAME),
                createCase("Update user email", UserField.EMAIL),
                createCase("Update user status", UserField.STATUS),
                createCase("Update user roles", UserField.ROLES)
        );
    }

    private UpdateUserTestCase createCase(String description, UserField field) {
        UpdateUserTestCase testCase = UpdateUserTestCase.builder()
                .field(field)
                .build();
        testCase.description(description);
        return testCase;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateUserTestCase extends TestCaseBase {
        private UserField field;

        public UpdateUserRequest buildRequest(GetUserResponse initialUser) {
            UpdateUserRequest.UpdateUserRequestBuilder builder = UserMapper.INSTANCE.toUpdateUserRequest(initialUser)
                    .toBuilder();

            switch (field) {
                case FIRST_NAME -> builder.firstName("UpdatedFirstName");
                case LAST_NAME -> builder.lastName("UpdatedLastName");
                case EMAIL -> builder.email("updated." + initialUser.getEmail());
                case STATUS -> builder.status(UserStatus.INACTIVE);
                case ROLES -> builder.roles(Set.of(UserRole.MENTOR));
            }

            return builder.build();
        }

        public void assertResult(GetUserResponse actualUser, UpdateUserRequest request) {
            switch (field) {
                case FIRST_NAME ->
                        UserAssertions.checkUserDataIsCorrect(actualUser, new UserAssertions.AssertionParams()
                                .firstName(request.getFirstName())
                                .displayName(UsersHelper.buildDisplayName(request.getFirstName(), request.getLastName())));
                case LAST_NAME -> UserAssertions.checkUserDataIsCorrect(actualUser, new UserAssertions.AssertionParams()
                        .lastName(request.getLastName())
                        .displayName(UsersHelper.buildDisplayName(request.getFirstName(), request.getLastName())));
                case EMAIL -> UserAssertions.checkUserDataIsCorrect(actualUser, new UserAssertions.AssertionParams()
                        .email(request.getEmail()));
                case STATUS -> UserAssertions.checkUserDataIsCorrect(actualUser, new UserAssertions.AssertionParams()
                        .status(request.getStatus().name()));
                case ROLES -> UserAssertions.checkUserDataIsCorrect(actualUser, new UserAssertions.AssertionParams()
                        .roles(List.copyOf(request.getRoles())));
            }
        }
    }

    public enum UserField {
        FIRST_NAME,
        LAST_NAME,
        EMAIL,
        STATUS,
        ROLES
    }
}
