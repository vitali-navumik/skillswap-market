package com.vitali.framework.utils;

import com.github.javafaker.Faker;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class FakerGenerator {

    private static final Faker FAKER = new Faker();
    private static final List<String> OFFER_CATEGORIES = List.of(
            "Testing",
            "QA Automation",
            "Java",
            "API Testing",
            "Career"
    );

    public static String randomEmail() {
        String first = FAKER.name().firstName().toLowerCase().replaceAll("[^a-z0-9]", "");
        String last = FAKER.name().lastName().toLowerCase().replaceAll("[^a-z0-9]", "");
        return String.format("%s.%s.%d.%s@test.local", first, last, System.currentTimeMillis(), UUID.randomUUID().toString().substring(0, 8));
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

    public static String randomOfferTitle() {
        return "Mentoring: " + FAKER.job().position() + " " + System.currentTimeMillis();
    }

    public static String randomOfferDescription() {
        return "Practice session on " + FAKER.job().keySkills() + " with hands-on examples and feedback.";
    }

    public static String randomOfferCategory() {
        return OFFER_CATEGORIES.get(ThreadLocalRandom.current().nextInt(OFFER_CATEGORIES.size()));
    }
}
