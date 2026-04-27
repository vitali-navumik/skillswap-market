package com.vitali.framework.ui.login;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LoginAssertions {
    private final LoginElements elements;

    public LoginAssertions(LoginElements elements) {
        this.elements = elements;
    }

    @Step("Check that login form is visible")
    public LoginAssertions loginFormIsVisible() {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(elements.emailInput().isVisible())
                .as("Email input should be visible")
                .isTrue();
        softly.assertThat(elements.passwordInput().isVisible())
                .as("Password input should be visible")
                .isTrue();
        softly.assertThat(elements.loginButton().isVisible())
                .as("Login button should be visible")
                .isTrue();
        softly.assertAll();
        return this;
    }

    @Step("Check that 'Log In' button disappeared")
    public LoginAssertions logInButtonDisappeared() {
        Locator logInButton = elements.loginButton();
        logInButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));
        assertThat(logInButton.isVisible())
                .as("Log In button should disappear")
                .isFalse();
        return this;
    }

    @Step("Check login error: {expectedMessage}")
    public LoginAssertions errorMessageIs(String expectedMessage) {
        Locator errorAlert = elements.errorAlert();
        errorAlert.waitFor();
        assertThat(errorAlert.innerText())
                .as("Login error message")
                .isEqualTo(expectedMessage);
        return this;
    }
}
