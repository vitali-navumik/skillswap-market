package com.vitali.framework.ui;

import com.microsoft.playwright.Page;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Application {
    private final Page page;
    private final Map<Class<?>, Object> pages = new ConcurrentHashMap<>();

    public Application(Page page) {
        this.page = page;
    }

    public <T> T getPage(Class<T> pageClass) {
        Object pageInstance = pages.computeIfAbsent(pageClass, cls -> {
            try {
                return cls.getConstructor(Page.class).newInstance(page);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not create an instance of " + cls.getSimpleName(), e);
            }
        });
        return pageClass.cast(pageInstance);
    }

    public void open(String url) {
        page.navigate(url);
    }
}
