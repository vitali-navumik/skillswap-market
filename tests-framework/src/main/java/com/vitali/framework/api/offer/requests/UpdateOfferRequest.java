package com.vitali.framework.api.offer.requests;

import com.vitali.framework.enums.OfferStatus;
import lombok.Data;
import lombok.Builder;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class UpdateOfferRequest {
    private UUID publicId;
    private String title;
    private String description;
    private String category;
    private Integer durationMinutes;
    private Integer priceCredits;
    private OfferStatus status;
}
