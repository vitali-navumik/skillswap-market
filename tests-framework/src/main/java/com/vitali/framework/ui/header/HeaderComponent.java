package com.vitali.framework.ui.header;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class HeaderComponent {
    private final HeaderElements elements;
    private final HeaderAssertions assertions;

    public HeaderComponent(Page page) {
        this.elements = new HeaderElements(page);
        this.assertions = new HeaderAssertions(elements);
    }

    public HeaderAssertions assertions() {
        return assertions;
    }

    @Step("Open user menu")
    public HeaderComponent openUserMenu() {
        elements.userPill().click();
        return this;
    }

    @Step("Log out")
    public HeaderComponent logout() {
        openUserMenu();
        elements.logoutButton().click();
        return this;
    }
}
