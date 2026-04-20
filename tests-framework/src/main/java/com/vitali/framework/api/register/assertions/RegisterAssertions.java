package com.vitali.framework.api.register.assertions;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.api.register.responses.RegisterUserResponse;
import com.vitali.framework.enums.UserRole;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.SoftAssertions;

import java.util.Set;

public class RegisterAssertions {

    @Step("Check registration data is correct")
    public static void checkRegistrationDataIsCorrect(RegisterUserResponse response, AssertionParams params) {
        SoftAssertions softly = new SoftAssertions();

        if (params.idExpected() != null && params.idExpected()) {
            softly.assertThat(response.getId())
                    .as("Registered user id is correct")
                    .isNotNull();
        }

        if (params.publicIdExpected() != null && params.publicIdExpected()) {
            softly.assertThat(response.getPublicId())
                    .as("Registered user public id is correct")
                    .isNotNull();
        }

        if (params.email() != null) {
            softly.assertThat(response.getEmail())
                    .as("Registered user email is correct")
                    .isEqualTo(params.email());
        }

        if (params.roles() != null) {
            softly.assertThat(response.getRoles())
                    .as("Registered user roles are correct")
                    .containsExactlyInAnyOrderElementsOf(params.roles());
        }

        if (params.status() != null) {
            softly.assertThat(String.valueOf(response.getStatus()))
                    .as("Registered user status is correct")
                    .isEqualTo(params.status());
        }

        softly.assertAll();
    }

    @Step("Check registration validation error")
    public static void checkRegistrationValidationError(ConnectorResponse<RegisterUserResponse> response,
                                                        ValidationErrorParams params) {
        CommonAssertions.checkBadRequest(response);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getDataResponse())
                .as("Registration validation error response contains common message")
                .contains("Validation failed");

        if (params.field() != null) {
            softly.assertThat(response.getDataResponse())
                    .as("Registration validation error contains field")
                    .contains("\"field\":\"" + params.field() + "\"");
        }

        if (params.message() != null) {
            softly.assertThat(response.getDataResponse())
                    .as("Registration validation error contains message")
                    .contains(params.message());
        }

        softly.assertAll();
    }

    @Data
    @Accessors(fluent = true)
    public static class AssertionParams {
        private Boolean idExpected;
        private Boolean publicIdExpected;
        private String email;
        private Set<UserRole> roles;
        private String status;
    }

    @Data
    @Accessors(fluent = true)
    public static class ValidationErrorParams {
        private String field;
        private String message;
    }
}
