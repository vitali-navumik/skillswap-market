package com.vitali.framework.utils;

import com.github.javafaker.Faker;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class FakerGenerator {

    private static final Faker FAKER = new Faker();

    public static String randomEmail() {
        String first = FAKER.name().firstName().toLowerCase().replaceAll("[^a-z0-9]", "");
        String last = FAKER.name().lastName().toLowerCase().replaceAll("[^a-z0-9]", "");
        return String.format("%s.%s.%d@test.local", first, last, System.currentTimeMillis());
    }

    public static String randomPassword() {
        return "StrongPass1";
    }

    public static String randomFirstName() {
        return "Auto" + FAKER.name().firstName();
    }

    public static String randomLastName() {
        return "Auto" + FAKER.name().lastName();
    }
}
