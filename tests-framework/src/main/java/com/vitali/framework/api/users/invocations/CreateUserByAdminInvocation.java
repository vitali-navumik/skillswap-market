package com.vitali.framework.api.users.invocations;

import com.vitali.framework.api.users.assertions.UserAssertions;
import com.vitali.framework.api.users.requests.CreateUserRequest;
import com.vitali.framework.api.users.responses.CreateUserResponse;
import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.enums.UserStatus;
import com.vitali.framework.invocations.GenericAPITestTemplateInvocationContextProvider;
import com.vitali.framework.invocations.TestCaseBase;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CreateUserByAdminInvocation implements GenericAPITestTemplateInvocationContextProvider<CreateUserByAdminInvocation.CreateUserTestCase> {

    @Override
    public Stream<CreateUserTestCase> getTestCasesStream() throws IOException {
        return Stream.of(
                createCase("Create student user", Set.of(UserRole.STUDENT), true),
                createCase("Create mentor user", Set.of(UserRole.MENTOR), true),
                createCase("Create admin user", Set.of(UserRole.ADMIN), false)
        );
    }

    private CreateUserTestCase createCase(String description, Set<UserRole> roles, boolean walletExpected) {
        CreateUserTestCase testCase = CreateUserTestCase.builder()
                .roles(roles)
                .expectedStatus(UserStatus.ACTIVE)
                .walletExpected(walletExpected)
                .build();
        testCase.description(description);
        return testCase;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @EqualsAndHashCode(callSuper = true)
    public static class CreateUserTestCase extends TestCaseBase {
        private Set<UserRole> roles;
        private UserStatus expectedStatus;
        private Boolean walletExpected;

        public CreateUserRequest buildRequest() {
            return CreateUserRequest.builder()
                    .roles(roles)
                    .status(expectedStatus)
                    .build();
        }

        public void assertResult(GetUserResponse createdUser, CreateUserRequest request) {
            UserAssertions.checkUserDataIsCorrect(createdUser, new UserAssertions.AssertionParams()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .roles(List.copyOf(roles))
                    .status(expectedStatus.name())
                    .walletExpected(walletExpected));
        }
    }
}
