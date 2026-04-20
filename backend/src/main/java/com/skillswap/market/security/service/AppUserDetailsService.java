package com.skillswap.market.security.service;

import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(username)
                .map(user -> {
                    if (!user.isActive()) {
                        throw new DisabledException("User is not active");
                    }
                    return AppUserPrincipal.from(user);
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
