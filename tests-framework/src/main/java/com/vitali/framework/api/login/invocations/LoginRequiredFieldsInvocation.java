package com.vitali.framework.api.login.invocations;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.api.login.response.LoginResponse;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.invocations.GenericAPITestTemplateInvocationContextProvider;
import com.vitali.framework.invocations.TestCaseBase;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginRequiredFieldsInvocation implements GenericAPITestTemplateInvocationContextProvider<LoginRequiredFieldsInvocation.LoginRequiredFieldTestCase> {

    @Override
    public Stream<LoginRequiredFieldTestCase> getTestCasesStream() {
        return Stream.of(
                createCase("Login without email", RequiredField.EMAIL),
                createCase("Login without password", RequiredField.PASSWORD),
                createCase("Login without email and password", RequiredField.EMAIL_AND_PASSWORD)
        );
    }

    private LoginRequiredFieldTestCase createCase(String description, RequiredField field) {
        LoginRequiredFieldTestCase testCase = LoginRequiredFieldTestCase.builder()
                .field(field)
                .build();
        testCase.description(description);
        return testCase;
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    @EqualsAndHashCode(callSuper = true)
    public static class LoginRequiredFieldTestCase extends TestCaseBase {
        private RequiredField field;

        public String email() {
            return switch (field) {
                case EMAIL, EMAIL_AND_PASSWORD -> "";
                case PASSWORD -> "user." + System.nanoTime() + "@example.com";
            };
        }

        public String password() {
            return switch (field) {
                case PASSWORD, EMAIL_AND_PASSWORD -> "";
                case EMAIL -> "StrongPass1";
            };
        }

        public void assertResult(ConnectorResponse<LoginResponse> response) {
            CommonAssertions.checkBadRequest(response);
            assertThat(response.getDataResponse())
                    .as("Login validation error contains field")
                    .contains("\"field\":\"" + field.primaryFieldName() + "\"");

            if (field.secondaryFieldName() != null) {
                assertThat(response.getDataResponse())
                        .as("Login validation error contains secondary field")
                        .contains("\"field\":\"" + field.secondaryFieldName() + "\"");
            }
        }
    }

    public enum RequiredField {
        EMAIL("email"),
        PASSWORD("password"),
        EMAIL_AND_PASSWORD("email", "password");

        private final String primaryFieldName;
        private final String secondaryFieldName;

        RequiredField(String primaryFieldName) {
            this(primaryFieldName, null);
        }

        RequiredField(String primaryFieldName, String secondaryFieldName) {
            this.primaryFieldName = primaryFieldName;
            this.secondaryFieldName = secondaryFieldName;
        }

        public String primaryFieldName() {
            return primaryFieldName;
        }

        public String secondaryFieldName() {
            return secondaryFieldName;
        }
    }
}
