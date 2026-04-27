package offer;

import com.vitali.framework.CommonAssertions;
import com.vitali.framework.GuestActions;
import com.vitali.framework.api.offer.assertions.OfferAssertions;
import com.vitali.framework.api.offer.OfferActions;
import com.vitali.framework.api.offer.requests.CreateOfferRequest;
import com.vitali.framework.api.offer.responses.CreateOfferResponse;
import com.vitali.framework.api.offer.responses.GetOfferResponse;
import com.vitali.framework.api.users.providers.UserRoleProvider.RoleNotAllowedToCreateOffer;
import com.vitali.framework.connectors.ConnectorResponse;
import com.vitali.framework.connectors.RestAssuredConnector;
import com.vitali.framework.connectors.Sender;
import com.vitali.framework.enums.UserPreset;
import com.vitali.framework.resolvers.ActionsContainer;
import com.vitali.framework.resolvers.GlobalActionsParameterResolver;
import com.vitali.framework.resolvers.GlobalActionsPreset;
import com.vitali.framework.resolvers.UserCreationHelper;
import com.vitali.framework.tags.OfferTag;
import com.vitali.framework.utils.JwtTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@OfferTag
@ExtendWith({GlobalActionsParameterResolver.class})
class OfferCreationTests {

    private final GuestActions guestActions = GuestActions.create();

    @Test
    @DisplayName("Mentor can create offer")
    void mentorCanCreateOffer(@GlobalActionsPreset(UserPreset.MENTOR) ActionsContainer mentor) {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();

        UUID publicId = mentor.offerActions().createOfferResponse(offerRequest).getPublicId();

        GetOfferResponse offerCreated = mentor.offerActions().getOfferResponse(publicId);
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

        CreateOfferResponse firstOffer = mentor.offerActions().createOfferResponse(offerRequest, idempotencyKey);
        CreateOfferResponse secondOffer = mentor.offerActions().createOfferResponse(offerRequest, idempotencyKey);

        OfferAssertions.checkOfferDataIsCorrect(secondOffer, new OfferAssertions.AssertionParams()
                .id(firstOffer.getId())
                .publicId(firstOffer.getPublicId()));
    }

    @ParameterizedTest
    @EnumSource(RoleNotAllowedToCreateOffer.class)
    @DisplayName("User without mentor role cannot create offer")
    void userWithoutMentorRoleCannotCreateOffer(RoleNotAllowedToCreateOffer role) {
        ActionsContainer actor = resolveUserActions(role);
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();

        ConnectorResponse<CreateOfferResponse> response = actor.offerActions().createOffer(offerRequest);

        CommonAssertions.checkForbidden(response);
        assertThat(response.getDataResponse())
                .as("Only mentor can create an offer")
                .contains("MENTOR role is required to create an offer");
    }

    @Test
    @DisplayName("Guest cannot create offer without token")
    void guestCannotCreateOfferWithoutToken() {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();

        ConnectorResponse<CreateOfferResponse> response = guestActions.offerActions().createOffer(offerRequest);

        CommonAssertions.checkForbidden(response);
    }

    @Test
    @DisplayName("User cannot create offer with malformed token")
    void userCannotCreateOfferWithMalformedToken() {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();

        ConnectorResponse<CreateOfferResponse> response = offerActionsWithToken("invalid-token").createOffer(offerRequest);

        CommonAssertions.checkForbidden(response);
    }

    @Test
    @DisplayName("User cannot create offer with expired token")
    void userCannotCreateOfferWithExpiredToken(@GlobalActionsPreset(UserPreset.MENTOR) ActionsContainer mentor) {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();
        String expiredToken = JwtTokenGenerator.expiredToken(
                mentor.userInfo().getEmail(),
                mentor.userInfo().getId(),
                java.util.Set.of("MENTOR")
        );

        ConnectorResponse<CreateOfferResponse> response = offerActionsWithToken(expiredToken).createOffer(offerRequest);

        CommonAssertions.checkForbidden(response);
    }

    @Test
    @DisplayName("User cannot create offer with invalid token signature")
    void userCannotCreateOfferWithInvalidTokenSignature(@GlobalActionsPreset(UserPreset.MENTOR) ActionsContainer mentor) {
        CreateOfferRequest offerRequest = CreateOfferRequest.builder().build();
        String invalidSignatureToken = JwtTokenGenerator.tokenWithInvalidSignature(
                mentor.userInfo().getEmail(),
                mentor.userInfo().getId(),
                java.util.Set.of("MENTOR")
        );

        ConnectorResponse<CreateOfferResponse> response = offerActionsWithToken(invalidSignatureToken).createOffer(offerRequest);

        CommonAssertions.checkForbidden(response);
    }

    private ActionsContainer resolveUserActions(RoleNotAllowedToCreateOffer role) {
        return switch (role) {
            case STUDENT, ADMIN -> UserCreationHelper.createUserAndLogIn(UserPreset.valueOf(role.name()));
        };
    }

    private OfferActions offerActionsWithToken(String token) {
        return new OfferActions(new Sender(token, new RestAssuredConnector()));
    }
}
