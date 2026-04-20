package com.skillswap.market.booking.service;

import com.skillswap.market.booking.dto.BookingResponse;
import com.skillswap.market.booking.dto.CreateBookingRequest;
import com.skillswap.market.booking.entity.Booking;
import com.skillswap.market.booking.entity.BookingStatus;
import com.skillswap.market.booking.repository.BookingRepository;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.entity.AvailabilitySlot;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SlotStatus;
import com.skillswap.market.offer.repository.AvailabilitySlotRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.support.UserNameFormatter;
import com.skillswap.market.wallet.entity.TransactionType;
import com.skillswap.market.wallet.entity.Wallet;
import com.skillswap.market.wallet.service.WalletService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final WalletService walletService;

    @Transactional
    public BookingResponse createBooking(AppUserPrincipal principal, CreateBookingRequest request) {
        requireStudentOrAdminRole(principal);
        User student = resolveBookingStudent(principal, request.studentId());

        AvailabilitySlot slot = availabilitySlotRepository.findWithOfferByIdForUpdate(request.slotId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Slot not found"));

        if (slot.getOffer().getStatus() != OfferStatus.ACTIVE) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Only ACTIVE offers can be booked");
        }
        if (slot.getStatus() != SlotStatus.OPEN) {
            throw new ApiException(HttpStatus.CONFLICT, "Slot is already occupied");
        }
        if (slot.getOffer().getMentor().getId().equals(student.getId())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "You cannot book your own offer");
        }
        Wallet studentWallet = walletService.getWalletForUpdate(student.getId());
        if (studentWallet.getBalance() < slot.getOffer().getPriceCredits()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient balance");
        }

        Booking booking = Booking.builder()
                .slot(slot)
                .offer(slot.getOffer())
                .student(student)
                .mentor(slot.getOffer().getMentor())
                .status(BookingStatus.RESERVED)
                .priceCredits(slot.getOffer().getPriceCredits())
                .reservedAmount(slot.getOffer().getPriceCredits())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        slot.setStatus(SlotStatus.BOOKED);
        studentWallet.setBalance(studentWallet.getBalance() - savedBooking.getReservedAmount());

        availabilitySlotRepository.save(slot);
        walletService.recordTransaction(studentWallet, savedBooking, TransactionType.CHARGE, savedBooking.getReservedAmount());

        return toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(AppUserPrincipal principal) {
        if (principal.roles().contains(Role.ADMIN)) {
            return bookingRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return bookingRepository.findByStudentIdOrMentorIdOrderByCreatedAtDesc(principal.id(), principal.id())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByPublicId(AppUserPrincipal principal, UUID publicId) {
        Booking booking = bookingRepository.findOneByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        assertBookingAccessible(principal, booking);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(AppUserPrincipal principal, UUID publicId) {
        Booking booking = bookingRepository.findForUpdateByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        boolean isStudent = booking.getStudent().getId().equals(principal.id());
        boolean isMentor = booking.getMentor().getId().equals(principal.id());
        if (!isStudent && !isMentor && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel this booking");
        }
        if (booking.getStatus() != BookingStatus.RESERVED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Booking cannot be cancelled from current status");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime slotStart = booking.getSlot().getStartTime();
        boolean wasReserved = booking.getStatus() == BookingStatus.RESERVED;
        if (wasReserved && !slotStart.isAfter(now)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Only future RESERVED bookings can be cancelled");
        }
        if (!wasReserved && !isMentor && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only mentor or admin can cancel a completed booking");
        }
        Wallet studentWallet = walletService.getWalletForUpdate(booking.getStudent().getId());
        Wallet mentorWallet;

        if (wasReserved) {
            studentWallet.setBalance(studentWallet.getBalance() + booking.getReservedAmount());
            walletService.recordTransaction(studentWallet, booking, TransactionType.REFUND, booking.getReservedAmount());
        } else {
            mentorWallet = walletService.getWalletForUpdate(booking.getMentor().getId());
            if (mentorWallet.getBalance() < booking.getReservedAmount()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Mentor balance is insufficient to cancel this completed booking");
            }
            studentWallet.setBalance(studentWallet.getBalance() + booking.getReservedAmount());
            mentorWallet.setBalance(mentorWallet.getBalance() - booking.getReservedAmount());
            walletService.recordTransaction(studentWallet, booking, TransactionType.REFUND, booking.getReservedAmount());
            walletService.recordTransaction(mentorWallet, booking, TransactionType.ADJUSTMENT, -booking.getReservedAmount());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledByUser(walletService.getUser(principal.id()));
        booking.setNoShowSide(null);

        if (wasReserved) {
            booking.getSlot().setStatus(SlotStatus.OPEN);
        }

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse completeBooking(AppUserPrincipal principal, UUID publicId) {
        Booking booking = bookingRepository.findForUpdateByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (!isAdmin && !booking.getMentor().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the mentor can complete this booking");
        }
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Only RESERVED bookings can be completed");
        }

        Wallet studentWallet = walletService.getWalletForUpdate(booking.getStudent().getId());
        Wallet mentorWallet = walletService.getWalletForUpdate(booking.getMentor().getId());
        int amount = booking.getReservedAmount();

        mentorWallet.setBalance(mentorWallet.getBalance() + amount);

        walletService.recordTransaction(mentorWallet, booking, TransactionType.PAYOUT, amount);

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setNoShowSide(null);
        return toResponse(booking);
    }

    private void assertBookingAccessible(AppUserPrincipal principal, Booking booking) {
        boolean isStudent = booking.getStudent().getId().equals(principal.id());
        boolean isMentor = booking.getMentor().getId().equals(principal.id());
        boolean isAdmin = principal.roles().contains(Role.ADMIN);

        if (!isStudent && !isMentor && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this booking");
        }
    }

    private void requireStudentOrAdminRole(AppUserPrincipal principal) {
        if (!principal.roles().contains(Role.STUDENT) && !principal.roles().contains(Role.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "STUDENT role is required to create a booking");
        }
    }

    private User resolveBookingStudent(AppUserPrincipal principal, Long studentId) {
        User student = walletService.getUser(studentId);
        boolean isAdmin = principal.roles().contains(Role.ADMIN);

        if (!student.getRoles().contains(Role.STUDENT)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Selected user must have the STUDENT role");
        }
        if (!student.isActive()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Selected student must be ACTIVE");
        }
        if (!isAdmin && !student.getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can create bookings only for yourself");
        }

        return student;
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getPublicId(),
                booking.getSlot().getId(),
                booking.getOffer().getId(),
                booking.getOffer().getPublicId(),
                booking.getOffer().getTitle(),
                booking.getStudent().getId(),
                booking.getStudent().getPublicId(),
                UserNameFormatter.format(booking.getStudent()),
                booking.getMentor().getId(),
                booking.getMentor().getPublicId(),
                UserNameFormatter.format(booking.getMentor()),
                booking.getStatus(),
                booking.getPriceCredits(),
                booking.getReservedAmount(),
                booking.getCancelledByUser() != null ? booking.getCancelledByUser().getId() : null,
                booking.getCancelledByUser() != null ? booking.getCancelledByUser().getPublicId() : null,
                booking.getSlot().getStartTime(),
                booking.getSlot().getEndTime(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
