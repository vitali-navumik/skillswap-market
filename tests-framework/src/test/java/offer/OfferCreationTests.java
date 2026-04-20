package offer;

import com.vitali.framework.api.offer.assertions.OfferAssertions;
import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.responses.OfferResponse;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.tags.OfferTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

@OfferTag
@ExtendWith({GlobalActionsParameterResolver.class})
class OfferCreationTests {

    @Test
    @DisplayName("Mentor can create offer")
    void mentorCanCreateOffer(@GlobalActionsPreset(UserPreset.MENTOR) ActionsContainer mentor) {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();

        UUID publicId = mentor.offerActions().createOfferResponse(offerRequest).getPublicId();

        OfferResponse offerCreated = mentor.offerActions().getOfferResponse(publicId);
        OfferAssertions.checkOfferDataIsCorrect(offerCreated, new OfferAssertions.AssertionParams()
                .mentorId(mentor.userInfo().getId())
                .mentorPublicId(mentor.userInfo().getPublicId())
                .title(offerRequest.getTitle())
                .description(offerRequest.getDescription())
                .category(offerRequest.getCategory())
                .durationMinutes(offerRequest.getDurationMinutes())
                .priceCredits(offerRequest.getPriceCredits())
                .status(offerRequest.getStatus())
                .publicIdExpected(true));
    }

    @Test
    @DisplayName("Mentor can safely retry offer creation with idempotency key")
    void mentorCanRetryCreateOfferWithSameIdempotencyKey(@GlobalActionsPreset(UserPreset.MENTOR) ActionsContainer mentor) {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();
        String idempotencyKey = UUID.randomUUID().toString();

        OfferResponse firstOffer = mentor.offerActions().createOfferResponse(offerRequest, idempotencyKey);
        OfferResponse secondOffer = mentor.offerActions().createOfferResponse(offerRequest, idempotencyKey);

        OfferAssertions.checkOfferDataIsCorrect(secondOffer, new OfferAssertions.AssertionParams()
                .id(firstOffer.getId())
                .publicId(firstOffer.getPublicId()));
    }
}
