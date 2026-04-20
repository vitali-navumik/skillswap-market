package com.skillswap.market.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.dto.CreateUserRequest;
import com.skillswap.market.user.dto.UpdateUserRequest;
import com.skillswap.market.user.dto.UserProfileResponse;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.repository.WalletRepository;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUserUpdatesOwnNamesAndReturnsFullName() {
        User user = user(5L, "Old", "Name", "Old Name");
        when(userRepository.findByPublicId(user.getPublicId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateUser(
                principal(5L, Role.STUDENT),
                user.getPublicId(),
                new UpdateUserRequest(null, "New", "Surname", null, "Custom Alias", null, null)
        );

        assertThat(response.firstName()).isEqualTo("New");
        assertThat(response.lastName()).isEqualTo("Surname");
        assertThat(response.displayName()).isEqualTo("New Surname");
    }

    @Test
    void blankDisplayNameFallsBackToFirstAndLastName() {
        User user = user(6L, "Initial", "User", "Initial User");
        when(userRepository.findByPublicId(user.getPublicId())).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(6L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateUser(
                principal(6L, Role.MENTOR),
                user.getPublicId(),
                new UpdateUserRequest(null, "Updated", "Person", null, "   ", null, null)
        );

        assertThat(response.displayName()).isEqualTo("Updated Person");
    }

    @Test
    void adminCanCreateStudentAndWalletIsCreated() {
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(9L);
            user.setStatus(UserStatus.ACTIVE);
            return user;
        });
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.findByUserId(9L)).thenReturn(Optional.empty());

        UserProfileResponse response = userService.createUser(
                principal(1L, Role.ADMIN),
                new CreateUserRequest("student@example.com", "StrongPass1", "Student", "Created", EnumSet.of(Role.STUDENT), UserStatus.ACTIVE)
        );

        assertThat(response.roles()).containsExactly(Role.STUDENT);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void adminCanCreatePureAdminWithoutWallet() {
        when(userRepository.findByEmailIgnoreCase("admin.created@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            user.setStatus(UserStatus.ACTIVE);
            return user;
        });
        when(walletRepository.findByUserId(10L)).thenReturn(Optional.empty());

        UserProfileResponse response = userService.createUser(
                principal(1L, Role.ADMIN),
                new CreateUserRequest("admin.created@example.com", "StrongPass1", "Admin", "Created", EnumSet.of(Role.ADMIN), UserStatus.ACTIVE)
        );

        assertThat(response.roles()).containsExactly(Role.ADMIN);
        assertThat(response.walletPublicId()).isNull();
    }

    @Test
    void createUserRejectsMultipleRoles() {
        assertThatThrownBy(() -> userService.createUser(
                principal(1L, Role.ADMIN),
                new CreateUserRequest("dual@example.com", "StrongPass1", "Dual", "Role", EnumSet.of(Role.STUDENT, Role.MENTOR), UserStatus.ACTIVE)
        )).isInstanceOf(com.skillswap.market.common.exception.ApiException.class);
    }

    private AppUserPrincipal principal(Long id, Role... roles) {
        Set<Role> roleSet = EnumSet.noneOf(Role.class);
        roleSet.addAll(java.util.List.of(roles));
        Set<GrantedAuthority> authorities = roleSet.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        return new AppUserPrincipal(id, "user@example.com", "encoded", true, roleSet, authorities);
    }

    private User user(Long id, String firstName, String lastName, String displayName) {
        return User.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .email("user@example.com")
                .firstName(firstName)
                .lastName(lastName)
                .displayName(displayName)
                .roles(EnumSet.of(Role.STUDENT))
                .build();
    }
}
