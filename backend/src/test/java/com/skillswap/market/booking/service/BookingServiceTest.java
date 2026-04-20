package com.skillswap.market.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillswap.market.booking.dto.BookingResponse;
import com.skillswap.market.booking.dto.CreateBookingRequest;
import com.skillswap.market.booking.entity.Booking;
import com.skillswap.market.booking.entity.BookingStatus;
import com.skillswap.market.booking.repository.BookingRepository;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.entity.AvailabilitySlot;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.entity.SlotStatus;
import com.skillswap.market.offer.repository.AvailabilitySlotRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.wallet.entity.TransactionType;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.service.WalletService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingRequiresStudentRole() {
        AppUserPrincipal mentorPrincipal = principal(2L, Role.MENTOR);

        assertThatThrownBy(() -> bookingService.createBooking(mentorPrincipal, new CreateBookingRequest(10L, 2L)))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void studentCannotCreateBookingForAnotherStudent() {
        when(walletService.getUser(11L)).thenReturn(user(11L, Role.STUDENT));

        assertThatThrownBy(() -> bookingService.createBooking(
                principal(10L, Role.STUDENT),
                new CreateBookingRequest(10L, 11L)
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminCanCreateBookingForSelectedStudent() {
        User selectedStudent = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(mentor, 80);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).plusDays(1), SlotStatus.OPEN);
        Wallet studentWallet = wallet(selectedStudent, 200, 0);

        when(walletService.getUser(selectedStudent.getId())).thenReturn(selectedStudent);
        when(availabilitySlotRepository.findWithOfferByIdForUpdate(77L)).thenReturn(java.util.Optional.of(slot));
        when(walletService.getWalletForUpdate(selectedStudent.getId())).thenReturn(studentWallet);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(88L);
            booking.setPublicId(UUID.randomUUID());
            booking.setCreatedAt(Instant.now());
            booking.setUpdatedAt(Instant.now());
            return booking;
        });

        BookingResponse response = bookingService.createBooking(
                principal(1L, Role.ADMIN),
                new CreateBookingRequest(77L, selectedStudent.getId())
        );

        assertThat(response.studentId()).isEqualTo(selectedStudent.getId());
        assertThat(response.mentorId()).isEqualTo(mentor.getId());
        assertThat(response.status()).isEqualTo(BookingStatus.RESERVED);
        assertThat(studentWallet.getBalance()).isEqualTo(120);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(walletService).recordTransaction(eq(studentWallet), any(Booking.class), eq(TransactionType.CHARGE), eq(80));
    }

    @Test
    void studentCannotBookOwnOffer() {
        User student = user(10L, Role.STUDENT);
        SkillOffer offer = offer(student, 80);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).plusDays(1), SlotStatus.OPEN);

        when(walletService.getUser(student.getId())).thenReturn(student);
        when(availabilitySlotRepository.findWithOfferByIdForUpdate(77L)).thenReturn(java.util.Optional.of(slot));

        assertThatThrownBy(() -> bookingService.createBooking(
                principal(student.getId(), Role.STUDENT),
                new CreateBookingRequest(77L, student.getId())
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(apiException.getMessage()).contains("cannot book your own offer");
                });
    }

    @Test
    void adminCannotCreateBookingForUserWithoutStudentRole() {
        when(walletService.getUser(20L)).thenReturn(user(20L, Role.MENTOR));

        assertThatThrownBy(() -> bookingService.createBooking(
                principal(1L, Role.ADMIN),
                new CreateBookingRequest(10L, 20L)
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void reservedCancellationReleasesFundsAndReopensFutureSlot() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(mentor, 80);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).plusHours(12), SlotStatus.BOOKED);
        Booking booking = booking(slot, offer, student, mentor, BookingStatus.RESERVED, 80);
        Wallet studentWallet = wallet(student, 120, 0);

        when(bookingRepository.findForUpdateByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));
        when(walletService.getWalletForUpdate(student.getId())).thenReturn(studentWallet);
        when(walletService.getUser(student.getId())).thenReturn(student);

        BookingResponse response = bookingService.cancelBooking(principal(student.getId(), Role.STUDENT), booking.getPublicId());

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(studentWallet.getBalance()).isEqualTo(200);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.OPEN);
        verify(walletService).recordTransaction(studentWallet, booking, TransactionType.REFUND, 80);
    }

    @Test
    void reservedCancellationFailsForPastSlot() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(mentor, 80);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).minusHours(2), SlotStatus.BOOKED);
        Booking booking = booking(slot, offer, student, mentor, BookingStatus.RESERVED, 80);

        when(bookingRepository.findForUpdateByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(principal(student.getId(), Role.STUDENT), booking.getPublicId()))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(apiException.getMessage()).contains("Only future RESERVED bookings can be cancelled");
                });
    }

    @Test
    void studentCannotCancelCompletedBooking() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(mentor, 80);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).plusHours(2), SlotStatus.BOOKED);
        Booking booking = booking(slot, offer, student, mentor, BookingStatus.COMPLETED, 80);

        when(bookingRepository.findForUpdateByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(principal(student.getId(), Role.STUDENT), booking.getPublicId()))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(apiException.getMessage()).contains("Only mentor or admin can cancel a completed booking");
                });
    }

    @Test
    void completeBookingCapturesReservedAmountAndPaysMentor() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(mentor, 120);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).plusDays(2), SlotStatus.BOOKED);
        Booking booking = booking(slot, offer, student, mentor, BookingStatus.RESERVED, 120);
        Wallet studentWallet = wallet(student, 80, 0);
        Wallet mentorWallet = wallet(mentor, 15, 0);

        when(bookingRepository.findForUpdateByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));
        when(walletService.getWalletForUpdate(student.getId())).thenReturn(studentWallet);
        when(walletService.getWalletForUpdate(mentor.getId())).thenReturn(mentorWallet);

        BookingResponse response = bookingService.completeBooking(principal(mentor.getId(), Role.MENTOR), booking.getPublicId());

        assertThat(response.status()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(studentWallet.getBalance()).isEqualTo(80);
        assertThat(mentorWallet.getBalance()).isEqualTo(135);
        verify(walletService).recordTransaction(mentorWallet, booking, TransactionType.PAYOUT, 120);
    }

    @Test
    void adminCanCancelCompletedBookingAndReverseFunds() {
        User admin = user(1L, Role.ADMIN);
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(mentor, 80);
        AvailabilitySlot slot = slot(offer, OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), SlotStatus.BOOKED);
        Booking booking = booking(slot, offer, student, mentor, BookingStatus.COMPLETED, 80);
        Wallet studentWallet = wallet(student, 120, 0);
        Wallet mentorWallet = wallet(mentor, 90, 0);

        when(bookingRepository.findForUpdateByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));
        when(walletService.getWalletForUpdate(student.getId())).thenReturn(studentWallet);
        when(walletService.getWalletForUpdate(mentor.getId())).thenReturn(mentorWallet);
        when(walletService.getUser(admin.getId())).thenReturn(admin);

        BookingResponse response = bookingService.cancelBooking(principal(admin.getId(), Role.ADMIN), booking.getPublicId());

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(studentWallet.getBalance()).isEqualTo(200);
        assertThat(mentorWallet.getBalance()).isEqualTo(10);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(walletService).recordTransaction(studentWallet, booking, TransactionType.REFUND, 80);
        verify(walletService).recordTransaction(mentorWallet, booking, TransactionType.ADJUSTMENT, -80);
    }

    private AppUserPrincipal principal(Long id, Role... roles) {
        Set<Role> roleSet = EnumSet.noneOf(Role.class);
        roleSet.addAll(java.util.List.of(roles));
        Set<GrantedAuthority> authorities = roleSet.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        return new AppUserPrincipal(id, "user@example.com", "encoded", true, roleSet, authorities);
    }

    private User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .email("user" + id + "@example.com")
                .firstName("User")
                .lastName(String.valueOf(id))
                .displayName("User " + id)
                .roles(EnumSet.of(role))
                .status(UserStatus.ACTIVE)
                .build();
    }

    private SkillOffer offer(User mentor, int priceCredits) {
        return SkillOffer.builder()
                .id(55L)
                .publicId(UUID.randomUUID())
                .mentor(mentor)
                .title("Offer")
                .description("desc")
                .category("Cat")
                .durationMinutes(60)
                .priceCredits(priceCredits)
                .cancellationPolicyHours(24)
                .status(OfferStatus.ACTIVE)
                .build();
    }

    private AvailabilitySlot slot(SkillOffer offer, OffsetDateTime startTime, SlotStatus status) {
        return AvailabilitySlot.builder()
                .id(77L)
                .offer(offer)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(offer.getDurationMinutes()))
                .status(status)
                .build();
    }

    private Booking booking(
            AvailabilitySlot slot,
            SkillOffer offer,
            User student,
            User mentor,
            BookingStatus status,
            int amount
    ) {
        return Booking.builder()
                .id(88L)
                .publicId(UUID.randomUUID())
                .slot(slot)
                .offer(offer)
                .student(student)
                .mentor(mentor)
                .status(status)
                .priceCredits(amount)
                .reservedAmount(amount)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Wallet wallet(User user, int balance, int reservedBalance) {
        return Wallet.builder()
                .id(user.getId() + 1000)
                .user(user)
                .balance(balance)
                .reservedBalance(reservedBalance)
                .build();
    }
}
