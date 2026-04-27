package com.vitali.framework.ui.header;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class HeaderElements {
    private final Page page;

    public HeaderElements(Page page) {
        this.page = page;
    }

    public Locator navigation() {
        return page.getByTestId("app-nav");
    }

    public Locator loginLink() {
        return page.getByTestId("nav-login");
    }

    public Locator catalogLink() {
        return page.getByTestId("nav-catalog");
    }

    public Locator userPill() {
        return page.getByTestId("topbar-user-pill");
    }

    public Locator userDropdown() {
        return page.getByTestId("topbar-user-dropdown");
    }

    public Locator logoutButton() {
        return page.getByTestId("logout-button");
    }
}
