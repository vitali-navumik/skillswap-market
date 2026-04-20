package com.skillswap.market.booking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillswap.market.booking.dto.BookingResponse;
import com.skillswap.market.booking.entity.BookingStatus;
import com.skillswap.market.security.jwt.JwtAuthenticationFilter;
import com.skillswap.market.booking.service.BookingService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void createBookingAcceptsSlotAndStudentPayload() throws Exception {
        UUID bookingPublicId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        UUID offerPublicId = UUID.fromString("22222222-2222-4222-8222-222222222222");
        UUID studentPublicId = UUID.fromString("33333333-3333-4333-8333-333333333333");
        UUID mentorPublicId = UUID.fromString("44444444-4444-4444-8444-444444444444");
        when(bookingService.createBooking(any(), any())).thenReturn(new BookingResponse(
                88L,
                bookingPublicId,
                77L,
                55L,
                offerPublicId,
                "Offer",
                10L,
                studentPublicId,
                "Student User",
                20L,
                mentorPublicId,
                "Mentor User",
                BookingStatus.RESERVED,
                80,
                80,
                null,
                null,
                OffsetDateTime.parse("2026-03-30T10:00:00Z"),
                OffsetDateTime.parse("2026-03-30T11:00:00Z"),
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:00:00Z")
        ));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 77,
                                  "studentId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(88))
                .andExpect(jsonPath("$.slotId").value(77))
                .andExpect(jsonPath("$.studentId").value(10))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void createBookingRejectsMissingStudentId() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 77
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingAcceptsMentorOfferForSelectedStudentPayload() throws Exception {
        UUID bookingPublicId = UUID.fromString("55555555-5555-4555-8555-555555555555");
        UUID offerPublicId = UUID.fromString("66666666-6666-4666-8666-666666666666");
        UUID studentPublicId = UUID.fromString("77777777-7777-4777-8777-777777777777");
        UUID mentorPublicId = UUID.fromString("88888888-8888-4888-8888-888888888888");
        when(bookingService.createBooking(any(), any())).thenReturn(new BookingResponse(
                99L,
                bookingPublicId,
                78L,
                56L,
                offerPublicId,
                "Mentor Demo Offer",
                10L,
                studentPublicId,
                "Student User",
                20L,
                mentorPublicId,
                "Mentor User",
                BookingStatus.RESERVED,
                60,
                60,
                null,
                null,
                OffsetDateTime.parse("2026-03-31T10:00:00Z"),
                OffsetDateTime.parse("2026-03-31T11:00:00Z"),
                Instant.parse("2026-03-24T11:00:00Z"),
                Instant.parse("2026-03-24T11:00:00Z")
        ));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 78,
                                  "studentId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.studentId").value(10))
                .andExpect(jsonPath("$.mentorId").value(20))
                .andExpect(jsonPath("$.offerTitle").value("Mentor Demo Offer"));
    }
}
