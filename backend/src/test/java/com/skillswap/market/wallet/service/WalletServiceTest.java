package com.skillswap.market.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.wallet.dto.TopUpWalletRequest;
import com.skillswap.market.wallet.dto.WalletResponse;
import com.skillswap.market.wallet.entity.TransactionType;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.entity.WalletTransaction;
import com.skillswap.market.wallet.repository.WalletRepository;
import com.skillswap.market.wallet.repository.WalletTransactionRepository;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void adminTopUpAddsBalanceAndRecordsTransaction() {
        UUID walletPublicId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        Wallet wallet = Wallet.builder()
                .id(5L)
                .publicId(walletPublicId)
                .user(user(7L))
                .balance(100)
                .reservedBalance(0)
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(walletRepository.findByPublicIdForUpdate(walletPublicId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.topUp(principal(1L, Role.ADMIN), walletPublicId, new TopUpWalletRequest(50));

        assertThat(response.balance()).isEqualTo(150);
        assertThat(response.reservedBalance()).isEqualTo(0);
        assertThat(response.availableBalance()).isEqualTo(150);

        ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(walletTransactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getType()).isEqualTo(TransactionType.TOP_UP);
        assertThat(transactionCaptor.getValue().getAmount()).isEqualTo(50);
    }

    @Test
    void topUpRejectsPureMentorWithoutStudentRole() {
        User mentor = user(7L);
        mentor.setRoles(EnumSet.of(Role.MENTOR));
        Wallet wallet = Wallet.builder()
                .id(5L)
                .publicId(UUID.fromString("22222222-2222-4222-8222-222222222222"))
                .user(mentor)
                .balance(100)
                .reservedBalance(0)
                .build();
        when(walletRepository.findByPublicIdForUpdate(wallet.getPublicId())).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.topUp(principal(7L, Role.MENTOR), wallet.getPublicId(), new TopUpWalletRequest(50)))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void topUpAllowsStudentRole() {
        UUID walletPublicId = UUID.fromString("33333333-3333-4333-8333-333333333333");
        Wallet wallet = Wallet.builder()
                .id(8L)
                .publicId(walletPublicId)
                .user(user(9L))
                .balance(40)
                .reservedBalance(0)
                .build();

        when(walletRepository.findByPublicIdForUpdate(walletPublicId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.topUp(principal(9L, Role.STUDENT), walletPublicId, new TopUpWalletRequest(60));

        assertThat(response.balance()).isEqualTo(100);
        assertThat(response.reservedBalance()).isEqualTo(0);
        assertThat(response.availableBalance()).isEqualTo(100);
    }

    @Test
    void getWalletResponseCalculatesAvailableBalance() {
        Wallet wallet = Wallet.builder()
                .id(6L)
                .user(user(8L))
                .balance(200)
                .reservedBalance(0)
                .build();

        when(walletRepository.findByUserId(8L)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getWalletResponse(8L);

        assertThat(response.balance()).isEqualTo(200);
        assertThat(response.reservedBalance()).isEqualTo(0);
        assertThat(response.availableBalance()).isEqualTo(200);
    }

    private AppUserPrincipal principal(Long id, Role... roles) {
        Set<Role> roleSet = EnumSet.noneOf(Role.class);
        roleSet.addAll(java.util.List.of(roles));
        Set<GrantedAuthority> authorities = roleSet.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        return new AppUserPrincipal(id, "user@example.com", "encoded", true, roleSet, authorities);
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .displayName("Test User")
                .roles(EnumSet.of(Role.STUDENT))
                .build();
    }
}
