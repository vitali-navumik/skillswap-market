package com.vitali.framework.api.auth.assertions;

import com.vitali.framework.api.auth.responses.RegisterResponse;
import com.vitali.framework.enums.UserRole;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.SoftAssertions;

import java.util.Set;

public class AuthAssertions {

    @Step("Check registration data is correct")
    public static void checkRegistrationDataIsCorrect(RegisterResponse response, AssertionParams params) {
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

    @Data
    @Accessors(fluent = true)
    public static class AssertionParams {
        private Boolean idExpected;
        private Boolean publicIdExpected;
        private String email;
        private Set<UserRole> roles;
        private String status;
    }
}
