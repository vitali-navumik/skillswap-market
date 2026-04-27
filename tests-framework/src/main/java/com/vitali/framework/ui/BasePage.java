package com.vitali.framework.ui;

import com.microsoft.playwright.Page;
import com.vitali.framework.ui.header.HeaderComponent;

public abstract class BasePage {
    protected final Page page;
    protected final HeaderComponent header;

    public BasePage(Page page) {
        this.page = page;
        this.header = new HeaderComponent(page);
    }

    public HeaderComponent header() {
        return header;
    }

    public void pause() {
        page.pause();
    }

    public abstract void waitForPageToLoad();
}
