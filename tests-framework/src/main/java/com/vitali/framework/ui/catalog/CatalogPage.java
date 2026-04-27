package com.vitali.framework.ui.catalog;

import com.microsoft.playwright.Page;
import com.vitali.framework.config.Config;
import com.vitali.framework.ui.BasePage;
import io.qameta.allure.Step;

public class CatalogPage extends BasePage {
    private final CatalogElements elements;
    private final CatalogAssertions assertions;

    public CatalogPage(Page page) {
        super(page);
        this.elements = new CatalogElements(page);
        this.assertions = new CatalogAssertions(elements, page);
    }

    public CatalogAssertions assertions() {
        return assertions;
    }

    @Step("Open catalog page")
    public CatalogPage open() {
        page.navigate(Config.UI_BASE_URL + "/offers");
        waitForPageToLoad();
        return this;
    }

    @Step("Search offers by keyword: {keyword}")
    public CatalogPage searchByKeyword(String keyword) {
        elements.searchInput().fill(keyword);
        elements.resultsSummary().waitFor();
        return this;
    }

    @Step("Select popular search: {keyword}")
    public CatalogPage selectPopularSearch(String keyword) {
        elements.popularSearch(keyword).click();
        elements.resultsSummary().waitFor();
        return this;
    }

    @Step("Reset catalog filters")
    public CatalogPage resetFilters() {
        elements.resetFiltersButton().click();
        return this;
    }

    @Override
    public void waitForPageToLoad() {
        elements.filtersPanel().waitFor();
        page.waitForCondition(() -> elements.resultsGrid().isVisible() || elements.emptyState().isVisible());
    }
}
