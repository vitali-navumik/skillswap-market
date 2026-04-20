package com.skillswap.market.booking.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long slotId,
        @NotNull Long studentId
) {
}
