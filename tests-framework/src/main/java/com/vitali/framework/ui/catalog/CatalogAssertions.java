package com.vitali.framework.ui.catalog;

import com.microsoft.playwright.Page;
import com.vitali.framework.config.Config;
import com.vitali.framework.api.offer.responses.BaseOfferResponse;
import io.qameta.allure.Step;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CatalogAssertions {
    private final CatalogElements elements;
    private final Page page;

    public CatalogAssertions(CatalogElements elements, Page page) {
        this.elements = elements;
        this.page = page;
    }

    @Step("Check that current URL is catalog page")
    public CatalogAssertions currentUrlIsCatalogPage() {
        page.waitForURL(Config.UI_BASE_URL + "/offers");
        assertThat(page.url())
                .as("Current URL should be the catalog page")
                .isEqualTo(Config.UI_BASE_URL + "/offers");
        return this;
    }

    @Step("Check that catalog title is visible")
    public CatalogAssertions catalogTitleIsVisible() {
        assertThat(elements.pageTitle().innerText())
                .as("Catalog page title")
                .isEqualTo("Catalog");
        return this;
    }

    @Step("Check that offer is visible in catalog")
    public CatalogAssertions offerIsVisible(BaseOfferResponse offer) {
        var offerCard = elements.offerCard(offer.getId());
        offerCard.waitFor();
        assertThat(offerCard.innerText())
                .as("Offer card should contain created offer data")
                .contains(offer.getTitle())
                .contains(offer.getCategory())
                .contains(offer.getPriceCredits() + " cr")
                .contains(offer.getMentorDisplayName());
        return this;
    }
}
