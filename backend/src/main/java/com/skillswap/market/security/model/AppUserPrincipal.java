package com.skillswap.market.security.model;

import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AppUserPrincipal(
        Long id,
        String email,
        String password,
        boolean enabled,
        Set<Role> roles,
        Collection<GrantedAuthority> authorities
) implements UserDetails {

    public static AppUserPrincipal from(User user) {
        Set<GrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(Role::name)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

        return new AppUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                user.getRoles(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
