package com.vitali.framework.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.Objects;

public class PlaywrightManager {
    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();

    public static Page createPage() {
        Playwright pw = Playwright.create();
        playwright.set(pw);

        Browser br = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        browser.set(br);

        BrowserContext ctx = br.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
        context.set(ctx);

        Page pg = ctx.newPage();
        page.set(pg);
        return pg;
    }

    public static Page getPage() {
        return Objects.requireNonNull(page.get(), "Page object is not initialized. Please call createPage() first.");
    }

    public static void close() {
        if (page.get() != null) {
            page.get().close();
            page.remove();
        }

        if (context.get() != null) {
            context.get().close();
            context.remove();
        }

        if (browser.get() != null) {
            browser.get().close();
            browser.remove();
        }

        if (playwright.get() != null) {
            playwright.get().close();
            playwright.remove();
        }
    }
}
