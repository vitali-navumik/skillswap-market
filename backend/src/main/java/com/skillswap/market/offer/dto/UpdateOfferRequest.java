package com.skillswap.market.offer.dto;

import com.skillswap.market.offer.entity.OfferStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateOfferRequest(
        @NotNull UUID publicId,
        @Size(max = 200) String title,
        String description,
        @Size(max = 100) String category,
        @Min(1) Integer durationMinutes,
        @Min(1) Integer priceCredits,
        OfferStatus status
) {
}
