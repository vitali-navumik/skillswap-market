package com.vitali.framework.api.users.providers;

public final class UserRoleProvider {

    private UserRoleProvider() {
    }

    public enum RoleNotAllowedToManageUsers {
        STUDENT,
        MENTOR
    }
}
