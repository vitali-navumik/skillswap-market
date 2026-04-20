package com.skillswap.market.wallet.controller;

import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.wallet.dto.TopUpWalletRequest;
import com.skillswap.market.wallet.dto.WalletResponse;
import com.skillswap.market.wallet.dto.WalletTransactionResponse;
import com.skillswap.market.wallet.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets/{walletPublicId}")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public WalletResponse getWallet(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID walletPublicId
    ) {
        return walletService.getWallet(principal, walletPublicId);
    }

    @PostMapping("/top-up")
    public WalletResponse topUp(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID walletPublicId,
            @Valid @RequestBody TopUpWalletRequest request
    ) {
        return walletService.topUp(principal, walletPublicId, request);
    }

    @GetMapping("/transactions")
    public List<WalletTransactionResponse> getTransactions(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID walletPublicId
    ) {
        return walletService.getTransactions(principal, walletPublicId);
    }
}
