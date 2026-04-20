package com.skillswap.market.user.service;

import com.skillswap.market.booking.entity.Booking;
import com.skillswap.market.booking.entity.BookingStatus;
import com.skillswap.market.booking.repository.BookingRepository;
import com.skillswap.market.offer.entity.AvailabilitySlot;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.entity.SlotStatus;
import com.skillswap.market.offer.repository.AvailabilitySlotRepository;
import com.skillswap.market.offer.repository.SkillOfferRepository;
import com.skillswap.market.review.entity.Review;
import com.skillswap.market.review.repository.ReviewRepository;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.wallet.entity.TransactionType;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.repository.WalletRepository;
import com.skillswap.market.wallet.service.WalletService;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoScenarioSeeder {

    private final UserRepository userRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    @Transactional
    public void seedScenarios() {
        User mentorOne = getUser("mentor1@test.com");
        User mentorTwo = getUser("mentor2@test.com");
        User studentOne = getUser("student1@test.com");
        User studentTwo = getUser("student2@test.com");
        User reactMentor = getUser("mentor3@test.com");

        ensureMinimumBalance(studentOne, 220);
        ensureMinimumBalance(studentTwo, 180);
        ensureMinimumBalance(reactMentor, 180);

        SkillOffer sqlOffer = ensureOffer(
                mentorOne,
                "SQL Interview Coaching",
                "[Demo] SQL Interview Coaching",
                "Hands-on prep for SQL interviews with schema design, joins, and timed exercises.",
                "Databases",
                60,
                90,
                OfferStatus.ACTIVE
        );
        SkillOffer reactOffer = ensureOffer(
                reactMentor,
                "React Pair Debugging",
                "[Demo] React Pair Debugging",
                "Live bug-fixing session for React state, forms, and rendering issues.",
                "Frontend",
                60,
                70,
                OfferStatus.ACTIVE
        );
        ensureOffer(
                mentorTwo,
                "Career Strategy Intensive",
                "[Demo] Career Strategy Intensive",
                "Structured coaching around CV, portfolio, and interview positioning.",
                "Career",
                45,
                55,
                OfferStatus.DRAFT
        );
        ensureOffer(
                mentorOne,
                "Community Session",
                "[Demo] Community Session (Blocked Example)",
                "Admin-only moderation example offer used to preview blocked state handling.",
                "Operations",
                30,
                25,
                OfferStatus.BLOCKED
        );

        ensureFutureOpenSlots(sqlOffer, 3, List.of(1, 3, 6), LocalTime.of(10, 0));
        ensureFutureOpenSlots(reactOffer, 2, List.of(2, 5), LocalTime.of(14, 0));

        Booking completedReviewBooking = ensureCompletedBooking(
                sqlOffer,
                studentOne,
                mentorOne,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0)
        );
        ensureReview(
                completedReviewBooking,
                studentOne,
                mentorOne,
                5,
                "Clear structure, practical tasks, and very actionable interview feedback."
        );

        ensureReservedBooking(
                reactOffer,
                studentOne,
                reactMentor,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0)
        );
    }

    private SkillOffer ensureOffer(
            User mentor,
            String title,
            String legacyTitle,
            String description,
            String category,
            int durationMinutes,
            int priceCredits,
            OfferStatus status
    ) {
        SkillOffer offer = skillOfferRepository.findByMentorIdAndTitleIgnoreCase(mentor.getId(), title)
                .or(() -> skillOfferRepository.findByMentorIdAndTitleIgnoreCase(mentor.getId(), legacyTitle))
                .orElseGet(() -> SkillOffer.builder()
                        .mentor(mentor)
                        .title(title)
                        .description(description)
                        .category(category)
                        .durationMinutes(durationMinutes)
                        .priceCredits(priceCredits)
                        .cancellationPolicyHours(24)
                        .status(status)
                        .build());

        offer.setTitle(title);
        offer.setDescription(description);
        offer.setCategory(category);
        offer.setDurationMinutes(durationMinutes);
        offer.setPriceCredits(priceCredits);
        offer.setCancellationPolicyHours(24);
        offer.setStatus(status);
        return skillOfferRepository.save(offer);
    }

    private void ensureFutureOpenSlots(SkillOffer offer, int desiredCount, List<Integer> dayOffsets, LocalTime startTime) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        long currentCount = availabilitySlotRepository.findByOfferIdOrderByStartTimeAsc(offer.getId())
                .stream()
                .filter(slot -> slot.getStatus() == SlotStatus.OPEN && slot.getStartTime().isAfter(now))
                .count();

        for (int dayOffset : dayOffsets) {
            if (currentCount >= desiredCount) {
                return;
            }
            OffsetDateTime start = now.plusDays(dayOffset)
                    .withHour(startTime.getHour())
                    .withMinute(startTime.getMinute())
                    .withSecond(0)
                    .withNano(0);
            OffsetDateTime end = start.plusMinutes(offer.getDurationMinutes());
            if (findSlot(offer, start, end).isEmpty()) {
                availabilitySlotRepository.save(
                        AvailabilitySlot.builder()
                                .offer(offer)
                                .startTime(start)
                                .endTime(end)
                                .status(SlotStatus.OPEN)
                                .build()
                );
                currentCount++;
            }
        }
    }

    private Booking ensureCompletedBooking(SkillOffer offer, User student, User mentor, OffsetDateTime startTime) {
        Booking existing = findBooking(offer, student, BookingStatus.COMPLETED);
        if (existing != null) {
            return existing;
        }

        ensureMinimumBalance(student, offer.getPriceCredits());
        OffsetDateTime endTime = startTime.plusMinutes(offer.getDurationMinutes());
        AvailabilitySlot slot = findSlot(offer, startTime, endTime)
                .orElseGet(() -> availabilitySlotRepository.save(
                        AvailabilitySlot.builder()
                                .offer(offer)
                                .startTime(startTime)
                                .endTime(endTime)
                                .status(SlotStatus.BOOKED)
                                .build()
                ));

        slot.setStatus(SlotStatus.BOOKED);
        availabilitySlotRepository.save(slot);

        Wallet studentWallet = walletService.getWalletForUpdate(student.getId());
        Wallet mentorWallet = walletService.getWalletForUpdate(mentor.getId());
        int amount = offer.getPriceCredits();

        studentWallet.setBalance(studentWallet.getBalance() - amount);
        walletRepository.save(studentWallet);

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .slot(slot)
                        .offer(offer)
                        .student(student)
                        .mentor(mentor)
                        .status(BookingStatus.COMPLETED)
                        .priceCredits(amount)
                        .reservedAmount(amount)
                        .build()
        );

        walletService.recordTransaction(studentWallet, booking, TransactionType.CHARGE, amount);
        mentorWallet.setBalance(mentorWallet.getBalance() + amount);

        walletRepository.save(studentWallet);
        walletRepository.save(mentorWallet);
        walletService.recordTransaction(mentorWallet, booking, TransactionType.PAYOUT, amount);

        return booking;
    }

    private Booking ensureReservedBooking(SkillOffer offer, User student, User mentor, OffsetDateTime startTime) {
        Booking existing = findBooking(offer, student, BookingStatus.RESERVED);
        if (existing != null) {
            return existing;
        }

        ensureMinimumBalance(student, offer.getPriceCredits());
        OffsetDateTime endTime = startTime.plusMinutes(offer.getDurationMinutes());
        AvailabilitySlot slot = findSlot(offer, startTime, endTime)
                .orElseGet(() -> availabilitySlotRepository.save(
                        AvailabilitySlot.builder()
                                .offer(offer)
                                .startTime(startTime)
                                .endTime(endTime)
                                .status(SlotStatus.BOOKED)
                                .build()
                ));

        slot.setStatus(SlotStatus.BOOKED);
        availabilitySlotRepository.save(slot);

        Wallet studentWallet = walletService.getWalletForUpdate(student.getId());
        int amount = offer.getPriceCredits();
        studentWallet.setBalance(studentWallet.getBalance() - amount);
        walletRepository.save(studentWallet);

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .slot(slot)
                        .offer(offer)
                        .student(student)
                        .mentor(mentor)
                        .status(BookingStatus.RESERVED)
                        .priceCredits(amount)
                        .reservedAmount(amount)
                        .build()
        );

        walletService.recordTransaction(studentWallet, booking, TransactionType.CHARGE, amount);
        return booking;
    }

    private void ensureReview(Booking booking, User author, User targetUser, int rating, String comment) {
        if (reviewRepository.existsByBookingIdAndAuthorId(booking.getId(), author.getId())) {
            return;
        }

        reviewRepository.save(
                Review.builder()
                        .offer(booking.getOffer())
                        .booking(booking)
                        .author(author)
                        .targetUser(targetUser)
                        .rating(rating)
                        .comment(comment)
                        .createdInAdminScope(false)
                        .build()
        );
    }

    private void ensureMinimumBalance(User user, int minimumBalance) {
        Wallet wallet = walletService.ensureWalletIfEligible(user)
                .orElseThrow(() -> new IllegalStateException("Seed balance requires wallet-eligible user"));
        if (wallet.getBalance() >= minimumBalance) {
            return;
        }

        int topUpAmount = minimumBalance - wallet.getBalance();
        wallet.setBalance(minimumBalance);
        walletRepository.save(wallet);
        walletService.recordTransaction(wallet, null, TransactionType.TOP_UP, topUpAmount);
    }

    private User getUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Demo user missing: " + email));
    }

    private Optional<AvailabilitySlot> findSlot(SkillOffer offer, OffsetDateTime startTime, OffsetDateTime endTime) {
        return availabilitySlotRepository.findByOfferIdOrderByStartTimeAsc(offer.getId())
                .stream()
                .filter(slot -> slot.getStartTime().isEqual(startTime) && slot.getEndTime().isEqual(endTime))
                .findFirst();
    }

    private Booking findBooking(SkillOffer offer, User student, BookingStatus status) {
        return bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getOffer().getId().equals(offer.getId()))
                .filter(booking -> booking.getStudent().getId().equals(student.getId()))
                .filter(booking -> booking.getStatus() == status)
                .findFirst()
                .orElse(null);
    }
}
