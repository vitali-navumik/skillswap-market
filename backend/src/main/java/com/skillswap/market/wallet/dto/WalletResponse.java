package com.skillswap.market.wallet.dto;

import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        Long id,
        UUID publicId,
        Long userId,
        UUID userPublicId,
        Integer balance,
        Integer reservedBalance,
        Integer availableBalance,
        Instant updatedAt
) {
}
