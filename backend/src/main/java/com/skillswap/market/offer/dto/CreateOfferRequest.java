package com.skillswap.market.offer.dto;

import com.skillswap.market.offer.entity.OfferStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOfferRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        @NotBlank @Size(max = 100) String category,
        @Min(1) Integer durationMinutes,
        @Min(1) Integer priceCredits,
        OfferStatus status
) {
}
