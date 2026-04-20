package com.skillswap.market.booking.dto;

import com.skillswap.market.booking.entity.NoShowSide;
import jakarta.validation.constraints.NotNull;

public record MarkNoShowRequest(
        @NotNull NoShowSide side
) {
}
