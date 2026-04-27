package com.vitali.framework.ui.login;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginElements {
    private final Page page;

    public LoginElements(Page page) {
        this.page = page;
    }

    public Locator emailInput() {
        return page.getByTestId("login-email-input");
    }

    public Locator passwordInput() {
        return page.getByTestId("login-password-input");
    }

    public Locator loginButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log In"));
    }

    public Locator errorAlert() {
        return page.locator(".alert.error");
    }
}
