package com.skillswap.market.user.service;

import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.dto.CreateUserRequest;
import com.skillswap.market.user.dto.UpdateUserRequest;
import com.skillswap.market.user.dto.UserProfileResponse;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.user.support.UserNameFormatter;
import com.skillswap.market.wallet.repository.WalletRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfileResponse createUser(AppUserPrincipal principal, CreateUserRequest request) {
        requireAdmin(principal.roles().contains(Role.ADMIN));

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
                });

        validateAssignedRoles(request.roles());
        validatePassword(request.password());

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .displayName(UserNameFormatter.format(request.firstName(), request.lastName(), null))
                .roles(EnumSet.copyOf(request.roles()))
                .status(request.status() == null ? UserStatus.ACTIVE : request.status())
                .build();

        User savedUser = userRepository.save(user);
        if (supportsWallet(savedUser)) {
            walletRepository.save(com.skillswap.market.wallet.entity.Wallet.builder()
                    .user(savedUser)
                    .balance(0)
                    .reservedBalance(0)
                    .build());
        }
        return toProfileResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getMentors() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRoles().contains(Role.MENTOR))
                .map(this::toProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getStudents() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRoles().contains(Role.STUDENT))
                .map(this::toProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(AppUserPrincipal principal, UUID publicId) {
        return toProfileResponse(findAccessibleUser(principal, publicId));
    }

    @Transactional
    public UserProfileResponse updateUser(AppUserPrincipal principal, UpdateUserRequest request) {
        User user = findAccessibleUser(principal, request.publicId());
        boolean isAdmin = principal.roles().contains(Role.ADMIN);

        if (request.email() != null) {
            requireNonBlank(request.email(), "Email cannot be blank");
            String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
            userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> {
                        throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
                    });
            user.setEmail(normalizedEmail);
        }

        if (request.firstName() != null) {
            requireNonBlank(request.firstName(), "First name cannot be blank");
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            requireNonBlank(request.lastName(), "Last name cannot be blank");
            user.setLastName(request.lastName().trim());
        }
        if (request.password() != null) {
            requireNonBlank(request.password(), "Password cannot be blank");
            validatePassword(request.password());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName().trim());
        } else if (request.displayName() != null) {
            user.setDisplayName(user.getFirstName() + " " + user.getLastName());
        }
        if (request.roles() != null) {
            requireAdmin(isAdmin);
            validateAssignedRoles(request.roles());
            user.setRoles(request.roles().isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(request.roles()));
        }
        if (request.status() != null) {
            requireAdmin(isAdmin);
            user.setStatus(request.status());
        }

        User savedUser = userRepository.save(user);
        return toProfileResponse(savedUser);
    }

    private User findAccessibleUser(AppUserPrincipal principal, UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (!isAdmin && !principal.id().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can access only your own profile");
        }
        return user;
    }

    private void requireAdmin(boolean isAdmin) {
        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only ADMIN may update these user fields");
        }
    }

    private void validateAssignedRoles(java.util.Set<Role> roles) {
        if (roles == null || roles.size() != 1) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Select exactly one role");
        }
    }

    public static boolean supportsWallet(User user) {
        return user.getRoles().contains(Role.STUDENT) || user.getRoles().contains(Role.MENTOR);
    }

    private void requireNonBlank(String value, String message) {
        if (value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
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

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getPublicId(),
                walletRepository.findByUserId(user.getId()).map(wallet -> wallet.getPublicId()).orElse(null),
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
