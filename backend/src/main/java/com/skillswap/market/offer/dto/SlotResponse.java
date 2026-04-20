package com.skillswap.market.offer.dto;

import com.skillswap.market.offer.entity.SlotStatus;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SlotResponse(
        Long id,
        Long offerId,
        UUID offerPublicId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        SlotStatus status,
        Instant createdAt
) {
}
