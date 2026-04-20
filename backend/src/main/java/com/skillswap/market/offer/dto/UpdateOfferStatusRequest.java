package com.skillswap.market.offer.dto;

import com.skillswap.market.offer.entity.OfferStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOfferStatusRequest(
        @NotNull OfferStatus status
) {
}
