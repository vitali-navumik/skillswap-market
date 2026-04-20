package com.skillswap.market.user.support;

import com.skillswap.market.user.entity.User;

public final class UserNameFormatter {

    private UserNameFormatter() {
    }

    public static String format(User user) {
        if (user == null) {
            return "";
        }
        return format(user.getFirstName(), user.getLastName(), user.getDisplayName());
    }

    public static String format(String firstName, String lastName, String fallback) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String fullName = (first + " " + last).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return fallback == null ? "" : fallback.trim();
    }
}
