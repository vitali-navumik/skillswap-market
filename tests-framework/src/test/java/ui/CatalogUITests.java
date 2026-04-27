package ui;

import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.responses.GetOfferResponse;
import com.vitali.framework.enums.OfferStatus;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.tags.UiTag;
import com.vitali.framework.ui.Application;
import com.vitali.framework.ui.ApplicationResolver;
import com.vitali.framework.ui.catalog.CatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

@UiTag
@ExtendWith({ApplicationResolver.class, GlobalActionsParameterResolver.class})
public class CatalogUITests {

    @Test
    @DisplayName("Guest can open catalog")
    void guestCanOpenCatalog(Application app) {
        app.getPage(CatalogPage.class)
                .open()
                .assertions()
                .currentUrlIsCatalogPage()
                .catalogTitleIsVisible();
    }

    @Test
    @DisplayName("Guest sees active offer in catalog")
    void guestSeesActiveOfferInCatalog(Application app,
                                       @GlobalActionsPreset(UserPreset.MENTOR) ActionsContainer mentor) {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder()
                .status(OfferStatus.ACTIVE)
                .build();
        UUID publicId = mentor.offerActions().createOfferResponse(offerRequest).getPublicId();

        GetOfferResponse offerCreated = mentor.offerActions().getOfferResponse(publicId);

        app.getPage(CatalogPage.class)
                .open()
                .assertions()
                .offerIsVisible(offerCreated);
    }
}
