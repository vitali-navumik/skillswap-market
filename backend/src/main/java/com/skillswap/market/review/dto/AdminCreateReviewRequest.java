package com.skillswap.market.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreateReviewRequest(
        @NotNull Long offerId,
        Long bookingId,
        Long targetUserId,
        @Min(1) @Max(5) Integer rating,
        @NotBlank String comment
) {
}
