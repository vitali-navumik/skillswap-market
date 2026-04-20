package com.skillswap.market.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillswap.market.auth.dto.RegisterRequest;
import com.skillswap.market.auth.dto.RegisterResponse;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.security.jwt.JwtService;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.service.WalletService;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserWithNormalizedEmailAndWallet() {
        RegisterRequest request = new RegisterRequest(
                "  USER@Example.COM ",
                "StrongPass1",
                "Ivan",
                "Petrov",
                EnumSet.of(Role.STUDENT)
        );

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            user.setStatus(UserStatus.ACTIVE);
            return user;
        });
        when(walletService.ensureWalletIfEligible(any(User.class))).thenReturn(Optional.of(Wallet.builder().balance(0).reservedBalance(0).build()));

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(walletService).ensureWalletIfEligible(captor.getValue());

        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getDisplayName()).isEqualTo("Ivan Petrov");
        assertThat(savedUser.getRoles()).containsExactly(Role.STUDENT);
        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void registerRejectsMultipleRolesInPublicRegistration() {
        RegisterRequest request = new RegisterRequest(
                "dual@example.com",
                "StrongPass1",
                "Dual",
                "Role",
                EnumSet.of(Role.STUDENT, Role.MENTOR)
        );

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(apiException.getMessage()).contains("Select exactly one role");
                });
    }

    @Test
    void registerRejectsAdminRoleInPublicRegistration() {
        RegisterRequest request = new RegisterRequest(
                "adminlike@example.com",
                "StrongPass1",
                "Admin",
                "Like",
                EnumSet.of(Role.ADMIN)
        );

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(apiException.getMessage()).contains("ADMIN role cannot be assigned");
                });
    }
}
