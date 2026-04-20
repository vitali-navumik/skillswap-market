package com.skillswap.market.booking.dto;

import com.skillswap.market.booking.entity.BookingStatus;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        Long id,
        UUID publicId,
        Long slotId,
        Long offerId,
        UUID offerPublicId,
        String offerTitle,
        Long studentId,
        UUID studentPublicId,
        String studentDisplayName,
        Long mentorId,
        UUID mentorPublicId,
        String mentorDisplayName,
        BookingStatus status,
        Integer priceCredits,
        Integer reservedAmount,
        Long cancelledByUserId,
        UUID cancelledByUserPublicId,
        OffsetDateTime slotStartTime,
        OffsetDateTime slotEndTime,
        Instant createdAt,
        Instant updatedAt
) {
}
