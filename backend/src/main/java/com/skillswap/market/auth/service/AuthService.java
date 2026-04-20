package com.skillswap.market.auth.service;

import com.skillswap.market.auth.dto.AuthResponse;
import com.skillswap.market.auth.dto.AuthUserResponse;
import com.skillswap.market.auth.dto.LoginRequest;
import com.skillswap.market.auth.dto.RegisterRequest;
import com.skillswap.market.auth.dto.RegisterResponse;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.security.jwt.JwtService;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.user.support.UserNameFormatter;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.service.WalletService;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final WalletService walletService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        validateRegistrationRoles(request.roles());
        validatePassword(request.password());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .displayName(UserNameFormatter.format(request.firstName(), request.lastName(), null))
                .roles(EnumSet.copyOf(request.roles()))
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        walletService.ensureWalletIfEligible(savedUser);
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getPublicId(),
                savedUser.getEmail(),
                savedUser.getRoles(),
                savedUser.getStatus()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(Locale.ROOT), request.password())
        );

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        Wallet wallet = walletService.ensureWalletIfEligible(user).orElse(null);

        return new AuthResponse(
                jwtService.generateToken(principal),
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                toAuthUserResponse(user, wallet != null ? wallet.getPublicId() : null)
        );
    }

    private void validateRegistrationRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "At least one role must be selected");
        }
        if (roles.size() != 1) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Select exactly one role");
        }
        if (roles.contains(Role.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN role cannot be assigned through public registration");
        }
        boolean hasProductRole = roles.contains(Role.STUDENT) || roles.contains(Role.MENTOR);
        if (!hasProductRole) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "At least one product role must be selected");
        }
    }

    private void validatePassword(String password) {
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!(hasUppercase && hasLowercase && hasDigit)) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            );
        }
    }

    private AuthUserResponse toAuthUserResponse(User user, java.util.UUID walletPublicId) {
        return new AuthUserResponse(
                user.getId(),
                user.getPublicId(),
                walletPublicId,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                UserNameFormatter.format(user),
                user.getRoles(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
