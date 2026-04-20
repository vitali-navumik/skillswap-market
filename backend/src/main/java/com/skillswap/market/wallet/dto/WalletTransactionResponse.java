package com.skillswap.market.wallet.dto;

import com.skillswap.market.wallet.entity.TransactionStatus;
import com.skillswap.market.wallet.entity.TransactionType;
import java.time.Instant;
import java.util.UUID;

public record WalletTransactionResponse(
        Long id,
        Long walletId,
        Long bookingId,
        UUID bookingPublicId,
        TransactionType type,
        Integer amount,
        TransactionStatus status,
        Instant createdAt
) {
}
