package com.vitali.framework.api.login.assertions;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.api.login.response.LoginResponse;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.SoftAssertions;

public class LoginAssertions {
    @Step("Check login response is correct")
    public static void checkLoginIsCorrect(LoginResponse response, AssertionParams params) {
        SoftAssertions softly = new SoftAssertions();

        if (params.accessTokenExpected() != null && params.accessTokenExpected()) {
            softly.assertThat(response.getAccessToken())
                    .as("Access token is correct")
                    .isNotNull();
        }

        if (params.tokenType() != null) {
            softly.assertThat(response.getTokenType())
                    .as("Token type is correct")
                    .isEqualTo(params.tokenType());
        }

        if (params.expiresInExpected() != null && params.expiresInExpected()) {
            softly.assertThat(response.getExpiresIn())
                    .as("Expires in is correct")
                    .isNotNull();
        }

        if (params.userExpected() != null && params.userExpected()) {
            softly.assertThat(response.getUser())
                    .as("User is correct")
                    .isNotNull();
        }

        if (params.email() != null) {
            softly.assertThat(response.getUser())
                    .as("Logged in user is present")
                    .isNotNull();
            softly.assertThat(response.getUser().getEmail())
                    .as("Logged in user email is correct")
                    .isEqualTo(params.email());
        }

        softly.assertAll();
    }

    @Step("Check wrong password login error")
    public static void checkWrongPasswordError(ConnectorResponse<LoginResponse> response) {
        CommonAssertions.checkUnauthorized(response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getDataResponse())
                .as("Wrong password returns error")
                .contains("Invalid email or password");
        softly.assertAll();
    }

    @Step("Check unknown email login error")
    public static void checkUnknownEmailError(ConnectorResponse<LoginResponse> response) {
        CommonAssertions.checkUnauthorized(response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getDataResponse())
                .as("Unknown email returns error")
                .contains("Invalid email or password");
        softly.assertAll();
    }

    @Step("Check inactive user login error")
    public static void checkInactiveUserError(ConnectorResponse<LoginResponse> response) {
        CommonAssertions.checkForbidden(response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getDataResponse())
                .as("Inactive user cannot log in message")
                .contains("User is not active");
        softly.assertAll();
    }

    @Step("Check blank credentials login validation error")
    public static void checkBlankCredentialsValidationError(ConnectorResponse<LoginResponse> response) {
        CommonAssertions.checkBadRequest(response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getDataResponse())
                .as("Blank credentials return validation error")
                .contains("\"field\":\"email\"");
        softly.assertThat(response.getDataResponse())
                .as("Blank credentials return validation error for password")
                .contains("\"field\":\"password\"");
        softly.assertAll();
    }

    @Step("Check invalid email format login validation error")
    public static void checkInvalidEmailFormatValidationError(ConnectorResponse<LoginResponse> response) {
        CommonAssertions.checkBadRequest(response);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getDataResponse())
                .as("Invalid email format returns validation error")
                .contains("\"field\":\"email\"");
        softly.assertThat(response.getDataResponse())
                .as("Invalid email format contains email validation message")
                .contains("must be a well-formed email address");
        softly.assertAll();
    }

    @Data
    @Accessors(fluent = true)
    public static class AssertionParams {
        private Boolean accessTokenExpected;
        private String tokenType;
        private Boolean expiresInExpected;
        private Boolean userExpected;
        private String email;
    }
}
