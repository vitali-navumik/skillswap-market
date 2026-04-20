package com.skillswap.market.wallet.service;

import com.skillswap.market.booking.entity.Booking;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.user.service.UserService;
import com.skillswap.market.wallet.dto.TopUpWalletRequest;
import com.skillswap.market.wallet.dto.WalletResponse;
import com.skillswap.market.wallet.dto.WalletTransactionResponse;
import com.skillswap.market.wallet.entity.TransactionStatus;
import com.skillswap.market.wallet.entity.TransactionType;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.entity.WalletTransaction;
import com.skillswap.market.wallet.repository.WalletRepository;
import com.skillswap.market.wallet.repository.WalletTransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Optional<Wallet> ensureWalletIfEligible(User user) {
        if (!UserService.supportsWallet(user)) {
            return Optional.empty();
        }
        return Optional.of(
                walletRepository.findByUserId(user.getId())
                        .orElseGet(() -> walletRepository.save(
                                Wallet.builder()
                                        .user(user)
                                        .balance(0)
                                        .reservedBalance(0)
                                        .build()
                        ))
        );
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(AppUserPrincipal principal, UUID walletPublicId) {
        Wallet wallet = getWalletByPublicId(walletPublicId);
        assertWalletAccess(principal, wallet.getUser().getId());
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse topUp(AppUserPrincipal principal, UUID walletPublicId, TopUpWalletRequest request) {
        Wallet wallet = getWalletForUpdate(walletPublicId);
        assertTopUpAllowed(principal, wallet.getUser().getId());
        wallet.setBalance(wallet.getBalance() + request.amount());
        Wallet savedWallet = walletRepository.save(wallet);
        recordTransaction(savedWallet, null, TransactionType.TOP_UP, request.amount());
        return toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactions(AppUserPrincipal principal, UUID walletPublicId) {
        Wallet wallet = getWalletByPublicId(walletPublicId);
        assertWalletAccess(principal, wallet.getUser().getId());
        return getTransactionsByUserId(wallet.getUser().getId());
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletResponse(Long userId) {
        return toResponse(getWallet(userId));
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactionsByUserId(Long userId) {
        Wallet wallet = getWallet(userId);
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public Wallet getWalletForUpdate(Long userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    @Transactional
    public Wallet getWalletForUpdate(UUID walletPublicId) {
        return walletRepository.findByPublicIdForUpdate(walletPublicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    @Transactional(readOnly = true)
    public Wallet getWalletByPublicId(UUID walletPublicId) {
        return walletRepository.findByPublicId(walletPublicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    @Transactional
    public void recordTransaction(Wallet wallet, Booking booking, TransactionType type, Integer amount) {
        walletTransactionRepository.save(
                WalletTransaction.builder()
                        .wallet(wallet)
                        .booking(booking)
                        .type(type)
                        .amount(amount)
                        .status(TransactionStatus.COMPLETED)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getPublicId(),
                wallet.getUser().getId(),
                wallet.getUser().getPublicId(),
                wallet.getBalance(),
                0,
                wallet.getBalance(),
                wallet.getUpdatedAt()
        );
    }

    private WalletTransactionResponse toTransactionResponse(WalletTransaction transaction) {
        return new WalletTransactionResponse(
                transaction.getId(),
                transaction.getWallet().getId(),
                transaction.getBooking() != null ? transaction.getBooking().getId() : null,
                transaction.getBooking() != null ? transaction.getBooking().getPublicId() : null,
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }

    private void requireStudentCapability(AppUserPrincipal principal) {
        if (!principal.roles().contains(Role.STUDENT)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "STUDENT role is required to top up wallet credits");
        }
    }

    private void assertWalletAccess(AppUserPrincipal principal, Long userId) {
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (!isAdmin && !principal.id().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can access only your own wallet");
        }
    }

    private void assertTopUpAllowed(AppUserPrincipal principal, Long userId) {
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (isAdmin) {
            User targetUser = getUser(userId);
            if (!targetUser.getRoles().contains(Role.STUDENT)) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Wallet top-up is supported only for users with the STUDENT role");
            }
            return;
        }

        if (!principal.id().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can top up only your own wallet");
        }
        requireStudentCapability(principal);
    }
}
