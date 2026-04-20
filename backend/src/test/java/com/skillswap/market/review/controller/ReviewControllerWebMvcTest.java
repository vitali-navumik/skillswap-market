package com.skillswap.market.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillswap.market.review.dto.ReviewResponse;
import com.skillswap.market.review.service.ReviewService;
import com.skillswap.market.security.jwt.JwtAuthenticationFilter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void createReviewAcceptsCompletedBookingReviewPayload() throws Exception {
        UUID bookingPublicId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        UUID reviewPublicId = UUID.fromString("22222222-2222-4222-8222-222222222222");
        UUID offerPublicId = UUID.fromString("33333333-3333-4333-8333-333333333333");
        UUID authorPublicId = UUID.fromString("44444444-4444-4444-8444-444444444444");
        UUID targetPublicId = UUID.fromString("55555555-5555-4555-8555-555555555555");
        when(reviewService.createReview(any(), eq(bookingPublicId), any())).thenReturn(new ReviewResponse(
                777L,
                reviewPublicId,
                30L,
                offerPublicId,
                "SQL Coaching",
                102L,
                bookingPublicId,
                10L,
                authorPublicId,
                "Student User",
                20L,
                targetPublicId,
                "Mentor User",
                5,
                "Helpful session",
                false,
                Instant.parse("2026-03-24T10:00:00Z"),
                Instant.parse("2026-03-24T10:00:00Z")
        ));

        mockMvc.perform(post("/api/bookings/{bookingPublicId}/reviews", bookingPublicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Helpful session"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(777))
                .andExpect(jsonPath("$.bookingId").value(102))
                .andExpect(jsonPath("$.authorId").value(10))
                .andExpect(jsonPath("$.targetUserId").value(20));
    }

    @Test
    void patchReviewAcceptsOwnReviewUpdatePayload() throws Exception {
        UUID reviewPublicId = UUID.fromString("66666666-6666-4666-8666-666666666666");
        UUID offerPublicId = UUID.fromString("77777777-7777-4777-8777-777777777777");
        UUID bookingPublicId = UUID.fromString("88888888-8888-4888-8888-888888888888");
        UUID authorPublicId = UUID.fromString("99999999-9999-4999-8999-999999999999");
        UUID targetPublicId = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
        when(reviewService.updateReview(any(), eq(reviewPublicId), any())).thenReturn(new ReviewResponse(
                77L,
                reviewPublicId,
                12L,
                offerPublicId,
                "SQL Coaching",
                91L,
                bookingPublicId,
                5L,
                authorPublicId,
                "Student User",
                9L,
                targetPublicId,
                "Mentor User",
                4,
                "Updated by student",
                false,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-02T10:00:00Z")
        ));

        mockMvc.perform(patch("/api/reviews/{reviewPublicId}", reviewPublicId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Updated by student"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.offerId").value(12))
                .andExpect(jsonPath("$.comment").value("Updated by student"));
    }

    @Test
    void deleteReviewExposesOwnReviewDeletionEndpoint() throws Exception {
        UUID reviewPublicId = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
        mockMvc.perform(delete("/api/reviews/{reviewPublicId}", reviewPublicId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getReviewsReturnsListPayload() throws Exception {
        UUID reviewPublicId = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
        UUID offerPublicId = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
        UUID authorPublicId = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
        UUID targetPublicId = UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");
        when(reviewService.getReviews(any())).thenReturn(List.of(new ReviewResponse(
                10L,
                reviewPublicId,
                5L,
                offerPublicId,
                "Admin Offer",
                null,
                null,
                1L,
                authorPublicId,
                "Admin User",
                8L,
                targetPublicId,
                "Mentor User",
                5,
                "Admin review",
                true,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-02T10:00:00Z")
        )));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].createdInAdminScope").value(true));
    }

    @Test
    void createDirectReviewAcceptsAdminPayload() throws Exception {
        UUID reviewPublicId = UUID.fromString("12121212-1212-4121-8121-121212121212");
        UUID offerPublicId = UUID.fromString("34343434-3434-4343-8343-343434343434");
        UUID authorPublicId = UUID.fromString("56565656-5656-4565-8565-565656565656");
        UUID targetPublicId = UUID.fromString("78787878-7878-4787-8787-787878787878");
        when(reviewService.createDirectReview(any(), any())).thenReturn(new ReviewResponse(
                11L,
                reviewPublicId,
                7L,
                offerPublicId,
                "Platform Review",
                null,
                null,
                1L,
                authorPublicId,
                "Admin User",
                12L,
                targetPublicId,
                "Offer Owner",
                5,
                "Editorial review",
                true,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-02T10:00:00Z")
        ));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "offerId": 7,
                                  "rating": 5,
                                  "comment": "Editorial review"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.offerId").value(7))
                .andExpect(jsonPath("$.createdInAdminScope").value(true));
    }
}
