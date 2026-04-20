package com.vitali.framework.api.offer.requests;

import com.vitali.framework.enums.OfferStatus;
import com.vitali.framework.utils.FakerGenerator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CreateOfferRequest {

    @Builder.Default
    private String title = FakerGenerator.randomOfferTitle();

    @Builder.Default
    private String description = FakerGenerator.randomOfferDescription();

    @Builder.Default
    private String category = FakerGenerator.randomOfferCategory();

    @Builder.Default
    private Integer durationMinutes = 60;

    @Builder.Default
    private Integer priceCredits = 25;

    @Builder.Default
    private OfferStatus status = OfferStatus.DRAFT;
}
