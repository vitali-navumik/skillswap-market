package com.vitali.framework.ui.login;

import com.microsoft.playwright.Page;
import com.vitali.framework.config.Config;
import com.vitali.framework.ui.BasePage;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {
    private final LoginElements elements;
    private final LoginAssertions assertions;

    public LoginPage(Page page) {
        super(page);
        this.elements = new LoginElements(page);
        this.assertions = new LoginAssertions(elements);
    }

    public LoginAssertions assertions() {
        return assertions;
    }

    @Step("Open login page")
    public LoginPage open() {
        page.navigate(Config.UI_BASE_URL + "/login");
        waitForPageToLoad();
        return this;
    }

    @Step("Enter email: {email}")
    public LoginPage enterEmail(String email) {
        elements.emailInput().fill(email);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        elements.passwordInput().fill(password);
        return this;
    }

    @Step("Click 'Log In' button")
    public LoginPage clickLoginButton() {
        elements.loginButton().click();
        return this;
    }

    @Step("Login as {email}")
    public LoginPage loginAs(String email, String password) {
        return open()
                .enterEmail(email)
                .enterPassword(password)
                .clickLoginButton();
    }

    @Override
    public void waitForPageToLoad() {
        elements.loginButton().waitFor();
    }
}
