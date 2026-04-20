package com.skillswap.market.user.service;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.wallet.entity.TransactionType;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.repository.WalletRepository;
import com.skillswap.market.wallet.repository.WalletTransactionRepository;
import com.skillswap.market.wallet.service.WalletService;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DemoDataInitializer {

    private static final String DEMO_PASSWORD = "StrongPass1";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final DemoScenarioSeeder demoScenarioSeeder;

    @Bean
    public ApplicationRunner seedDemoUsers() {
        return args -> {
            demoUsers().forEach(this::createOrUpdateDemoUser);
            demoScenarioSeeder.seedScenarios();
        };
    }

    private void createOrUpdateDemoUser(DemoUser demoUser) {
        String email = demoUser.email().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                                .firstName(demoUser.firstName())
                                .lastName(demoUser.lastName())
                                .displayName(demoUser.firstName() + " " + demoUser.lastName())
                                .roles(EnumSet.copyOf(demoUser.roles()))
                                .status(UserStatus.ACTIVE)
                                .build()
                ));

        if (!UserService.supportsWallet(user)) {
            return;
        }

        Wallet wallet = walletService.ensureWalletIfEligible(user)
                .orElseThrow(() -> new IllegalStateException("Demo data wallet seed requires wallet-eligible user"));
        boolean canSeedBalance = wallet.getBalance() == 0
                && wallet.getReservedBalance() == 0
                && walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).isEmpty();

        if (demoUser.initialBalance() > 0 && canSeedBalance) {
            wallet.setBalance(demoUser.initialBalance());
            walletRepository.save(wallet);
            walletService.recordTransaction(wallet, null, TransactionType.TOP_UP, demoUser.initialBalance());
        }
    }

    private List<DemoUser> demoUsers() {
        return List.of(
                new DemoUser("admin@test.com", "Admin", "User", Set.of(Role.ADMIN), 0),
                new DemoUser("mentor1@test.com", "Mentor", "One", Set.of(Role.MENTOR), 40),
                new DemoUser("mentor2@test.com", "Mentor", "Two", Set.of(Role.MENTOR), 30),
                new DemoUser("student1@test.com", "Student", "One", Set.of(Role.STUDENT), 150),
                new DemoUser("student2@test.com", "Student", "Two", Set.of(Role.STUDENT), 130),
                new DemoUser("mentor3@test.com", "Mentor", "Three", Set.of(Role.MENTOR), 150)
        );
    }

    private record DemoUser(
            String email,
            String firstName,
            String lastName,
            Set<Role> roles,
            int initialBalance
    ) {
    }
}
