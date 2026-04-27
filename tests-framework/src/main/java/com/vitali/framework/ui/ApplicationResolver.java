package com.vitali.framework.ui;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class ApplicationResolver implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
    private static final ThreadLocal<Application> app = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        Page page = PlaywrightManager.createPage();
        app.set(new Application(page));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        PlaywrightManager.close();
        app.remove();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(Application.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return app.get();
    }
}
