package com.skillswap.market.wallet.dto;

import jakarta.validation.constraints.Min;

public record TopUpWalletRequest(
        @Min(1) Integer amount
) {
}
