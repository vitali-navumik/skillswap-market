package com.vitali.framework.api.users.assertions;

import com.vitali.framework.api.users.responses.GetUserResponse;
import com.vitali.framework.enums.UserRole;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.SoftAssertions;

import java.util.Set;

public class UserAssertions {

    @Step("Check user data is correct")
    public static void checkUserDataIsCorrect(GetUserResponse user, AssertionParams params) {
        SoftAssertions softly = new SoftAssertions();

        if (params.email() != null) {
            softly.assertThat(user.getEmail())
                    .as("User email is correct")
                    .isEqualTo(params.email());
        }

        if (params.firstName() != null) {
            softly.assertThat(user.getFirstName())
                    .as("User first name is correct")
                    .isEqualTo(params.firstName());
        }

        if (params.lastName() != null) {
            softly.assertThat(user.getLastName())
                    .as("User last name is correct")
                    .isEqualTo(params.lastName());
        }

        if (params.displayName() != null) {
            softly.assertThat(user.getDisplayName())
                    .as("User display name is correct")
                    .isEqualTo(params.displayName());
        }

        if (params.status() != null) {
            softly.assertThat(String.valueOf(user.getStatus()))
                    .as("User status is correct")
                    .isEqualTo(params.status());
        }

        if (params.roles() != null) {
            softly.assertThat(user.getRoles())
                    .as("User roles are correct")
                    .containsExactlyInAnyOrderElementsOf(params.roles());
        }

        if (params.walletExpected() != null) {
            if (params.walletExpected()) {
                softly.assertThat(user.getWalletPublicId())
                        .as("User wallet public id is correct")
                        .isNotNull();
            } else {
                softly.assertThat(user.getWalletPublicId())
                        .as("User wallet public id is correct")
                        .isNull();
            }
        }

        softly.assertAll();
    }

    @Data
    @Accessors(fluent = true)
    public static class AssertionParams {
        private String email;
        private String firstName;
        private String lastName;
        private String displayName;
        private Set<UserRole> roles;
        private String status;
        private Boolean walletExpected;
    }
}
