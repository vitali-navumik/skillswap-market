package com.vitali.framework.api.register.invocations;

import com.vitali.framework.api.register.assertions.RegisterAssertions;
import com.vitali.framework.api.register.requests.RegisterUserRequest;
import com.vitali.framework.api.register.responses.RegisterUserResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.enums.UserRole;
import com.vitali.framework.invocations.GenericAPITestTemplateInvocationContextProvider;
import com.vitali.framework.invocations.TestCaseBase;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.stream.Stream;

public class RegisterUserRequiredFieldsInvocation implements GenericAPITestTemplateInvocationContextProvider<RegisterUserRequiredFieldsInvocation.RegisterRequiredFieldTestCase> {

    @Override
    public Stream<RegisterRequiredFieldTestCase> getTestCasesStream() {
        return Stream.of(
                createCase("Register user without email", RequiredField.EMAIL),
                createCase("Register user without password", RequiredField.PASSWORD),
                createCase("Register user without first name", RequiredField.FIRST_NAME),
                createCase("Register user without last name", RequiredField.LAST_NAME),
                createCase("Register user without roles", RequiredField.ROLES)
        );
    }

    private RegisterRequiredFieldTestCase createCase(String description, RequiredField field) {
        RegisterRequiredFieldTestCase testCase = RegisterRequiredFieldTestCase.builder()
                .field(field)
                .build();
        testCase.description(description);
        return testCase;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @EqualsAndHashCode(callSuper = true)
    public static class RegisterRequiredFieldTestCase extends TestCaseBase {
        private RequiredField field;

        public RegisterUserRequest buildRequest() {
            RegisterUserRequest.RegisterUserRequestBuilder builder = RegisterUserRequest.builder()
                    .roles(Set.of(UserRole.STUDENT));

            switch (field) {
                case EMAIL -> builder.email("");
                case PASSWORD -> builder.password("");
                case FIRST_NAME -> builder.firstName("");
                case LAST_NAME -> builder.lastName("");
                case ROLES -> builder.roles(Set.of());
            }

            return builder.build();
        }

        public void assertResult(ConnectorResponse<RegisterUserResponse> response) {
            RegisterAssertions.checkRegistrationValidationError(response, new RegisterAssertions.ValidationErrorParams()
                    .field(field.fieldName()));
        }
    }

    public enum RequiredField {
        EMAIL("email"),
        PASSWORD("password"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName"),
        ROLES("roles");

        private final String fieldName;

        RequiredField(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }
}
