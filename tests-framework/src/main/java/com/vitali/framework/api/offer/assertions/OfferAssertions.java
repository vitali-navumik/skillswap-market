package com.vitali.framework.api.offer.assertions;

import com.vitali.framework.api.offer.responses.BaseOfferResponse;
import com.vitali.framework.enums.OfferStatus;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.SoftAssertions;

import java.util.UUID;

public final class OfferAssertions {

    private OfferAssertions() {
    }

    @Step("Check offer data is correct")
    public static void checkOfferDataIsCorrect(BaseOfferResponse offer, AssertionParams params) {
        SoftAssertions softly = new SoftAssertions();

        if (params.idExpected() != null && params.idExpected()) {
            softly.assertThat(offer.getId())
                    .as("Offer id is generated")
                    .isNotNull();
        }

        if (params.id() != null) {
            softly.assertThat(offer.getId())
                    .as("Offer id is correct")
                    .isEqualTo(params.id());
        }

        if (params.publicIdExpected() != null && params.publicIdExpected()) {
            softly.assertThat(offer.getPublicId())
                    .as("Offer public id is generated")
                    .isNotNull();
        }

        if (params.publicId() != null) {
            softly.assertThat(offer.getPublicId())
                    .as("Offer public id is correct")
                    .isEqualTo(params.publicId());
        }

        if (params.mentorId() != null) {
            softly.assertThat(offer.getMentorId())
                    .as("Offer mentor id is correct")
                    .isEqualTo(params.mentorId());
        }

        if (params.mentorPublicId() != null) {
            softly.assertThat(offer.getMentorPublicId())
                    .as("Offer mentor public id is correct")
                    .isEqualTo(params.mentorPublicId());
        }

        if (params.title() != null) {
            softly.assertThat(offer.getTitle())
                    .as("Offer title is correct")
                    .isEqualTo(params.title());
        }

        if (params.description() != null) {
            softly.assertThat(offer.getDescription())
                    .as("Offer description is correct")
                    .isEqualTo(params.description());
        }

        if (params.category() != null) {
            softly.assertThat(offer.getCategory())
                    .as("Offer category is correct")
                    .isEqualTo(params.category());
        }

        if (params.durationMinutes() != null) {
            softly.assertThat(offer.getDurationMinutes())
                    .as("Offer duration is correct")
                    .isEqualTo(params.durationMinutes());
        }

        if (params.priceCredits() != null) {
            softly.assertThat(offer.getPriceCredits())
                    .as("Offer price is correct")
                    .isEqualTo(params.priceCredits());
        }

        if (params.status() != null) {
            softly.assertThat(offer.getStatus())
                    .as("Offer status is correct")
                    .isEqualTo(params.status());
        }

        softly.assertAll();
    }

    @Data
    @Accessors(fluent = true)
    public static class AssertionParams {
        private Long id;
        private Boolean idExpected;
        private UUID publicId;
        private Boolean publicIdExpected;
        private Long mentorId;
        private UUID mentorPublicId;
        private String title;
        private String description;
        private String category;
        private Integer durationMinutes;
        private Integer priceCredits;
        private OfferStatus status;
    }
}
