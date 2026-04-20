package com.skillswap.market.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        Long id,
        UUID publicId,
        Long offerId,
        UUID offerPublicId,
        String offerTitle,
        Long bookingId,
        UUID bookingPublicId,
        Long authorId,
        UUID authorPublicId,
        String authorDisplayName,
        Long targetUserId,
        UUID targetUserPublicId,
        String targetUserDisplayName,
        Integer rating,
        String comment,
        boolean createdInAdminScope,
        Instant createdAt,
        Instant updatedAt
) {
}
