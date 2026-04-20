package com.skillswap.market.booking.controller;

import com.skillswap.market.booking.dto.BookingResponse;
import com.skillswap.market.booking.dto.CreateBookingRequest;
import com.skillswap.market.booking.service.BookingService;
import com.skillswap.market.security.model.AppUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.createBooking(principal, request);
    }

    @GetMapping
    public List<BookingResponse> getBookings(@AuthenticationPrincipal AppUserPrincipal principal) {
        return bookingService.getBookings(principal);
    }

    @GetMapping("/{publicId}")
    public BookingResponse getBooking(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID publicId
    ) {
        return bookingService.getBookingByPublicId(principal, publicId);
    }

    @PostMapping("/{publicId}/cancel")
    public BookingResponse cancelBooking(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID publicId
    ) {
        return bookingService.cancelBooking(principal, publicId);
    }

    @PostMapping("/{publicId}/complete")
    public BookingResponse completeBooking(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID publicId
    ) {
        return bookingService.completeBooking(principal, publicId);
    }
}
