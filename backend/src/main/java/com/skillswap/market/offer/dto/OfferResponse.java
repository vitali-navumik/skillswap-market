package com.skillswap.market.offer.dto;

import com.skillswap.market.offer.entity.OfferStatus;
import java.time.Instant;
import java.util.UUID;

public record OfferResponse(
        Long id,
        UUID publicId,
        Long mentorId,
        UUID mentorPublicId,
        String mentorDisplayName,
        String title,
        String description,
        String category,
        Integer durationMinutes,
        Integer priceCredits,
        Integer cancellationPolicyHours,
        OfferStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
