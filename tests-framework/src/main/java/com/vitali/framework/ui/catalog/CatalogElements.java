package com.vitali.framework.ui.catalog;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CatalogElements {
    private final Page page;

    public CatalogElements(Page page) {
        this.page = page;
    }

    public Locator filtersPanel() {
        return page.getByTestId("catalog-filters-panel");
    }

    public Locator pageTitle() {
        return page.getByTestId("catalog-page-title");
    }

    public Locator searchInput() {
        return page.getByTestId("catalog-search-input");
    }

    public Locator resultsGrid() {
        return page.getByTestId("catalog-results-grid");
    }

    public Locator offerCards() {
        return page.locator("[data-testid^='offer-card-']");
    }

    public Locator offerCard(Long offerId) {
        return page.getByTestId("offer-card-" + offerId);
    }

    public Locator emptyState() {
        return page.getByTestId("catalog-empty-state");
    }

    public Locator resultsSummary() {
        return page.getByTestId("catalog-results-summary");
    }

    public Locator popularSearch(String search) {
        return page.getByTestId("catalog-popular-search-" + search.toLowerCase());
    }

    public Locator resetFiltersButton() {
        return page.getByTestId("catalog-reset-filters");
    }
}
