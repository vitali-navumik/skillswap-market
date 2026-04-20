package com.vitali.framework.api.users.helpers;

import com.vitali.framework.api.users.responses.GetUserResponse;

import java.util.List;
import java.util.UUID;

public final class UsersHelper {

    private UsersHelper() {
    }

    public static GetUserResponse getUserByPublicId(List<GetUserResponse> users, UUID publicId) {
        return users.stream()
                .filter(user -> user.getPublicId().equals(publicId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("User was not found in users list"));
    }

    public static String buildDisplayName(String firstName, String lastName) {
        return String.format("%s %s", firstName, lastName).trim();
    }
}
