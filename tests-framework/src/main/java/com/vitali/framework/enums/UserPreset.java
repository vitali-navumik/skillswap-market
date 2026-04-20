package com.vitali.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public enum UserPreset {
    ADMIN(Set.of(UserRole.ADMIN)),
    STUDENT(Set.of(UserRole.STUDENT)),
    MENTOR(Set.of(UserRole.MENTOR));

    private final Set<UserRole> roles;
}
