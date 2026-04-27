package com.vitali.framework.ui.header;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HeaderAssertions {
    private final HeaderElements elements;

    public HeaderAssertions(HeaderElements elements) {
        this.elements = elements;
    }

    @Step("Check that guest navigation is visible")
    public HeaderAssertions guestNavigationIsVisible() {
        assertThat(elements.loginLink().isVisible())
                .as("Login link should be visible for guest")
                .isTrue();
        return this;
    }

    @Step("Check that user menu contains name: {firstName} {lastName}")
    public HeaderAssertions userMenuContainsName(String firstName, String lastName) {
        Locator userPill = elements.userPill();
        userPill.waitFor();
        assertThat(userPill.innerText())
                .as("User menu should contain logged in user's name")
                .contains(firstName + " " + lastName);
        return this;
    }
}
