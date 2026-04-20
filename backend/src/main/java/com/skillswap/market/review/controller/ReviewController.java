package com.skillswap.market.review.controller;

import com.skillswap.market.review.dto.CreateReviewRequest;
import com.skillswap.market.review.dto.ReviewResponse;
import com.skillswap.market.review.dto.AdminCreateReviewRequest;
import com.skillswap.market.review.dto.UpdateReviewRequest;
import com.skillswap.market.review.service.ReviewService;
import com.skillswap.market.security.model.AppUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/reviews")
    public List<ReviewResponse> getReviews(@AuthenticationPrincipal AppUserPrincipal principal) {
        return reviewService.getReviews(principal);
    }

    @GetMapping("/api/reviews/{reviewPublicId}")
    public ReviewResponse getReview(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID reviewPublicId
    ) {
        return reviewService.getReview(principal, reviewPublicId);
    }

    @PostMapping("/api/bookings/{bookingPublicId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID bookingPublicId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return reviewService.createReview(principal, bookingPublicId, request);
    }

    @PostMapping("/api/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createDirectReview(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody AdminCreateReviewRequest request
    ) {
        return reviewService.createDirectReview(principal, request);
    }

    @PatchMapping("/api/reviews/{reviewPublicId}")
    public ReviewResponse updateReview(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID reviewPublicId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return reviewService.updateReview(principal, reviewPublicId, request);
    }

    @DeleteMapping("/api/reviews/{reviewPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID reviewPublicId
    ) {
        reviewService.deleteReview(principal, reviewPublicId);
    }

    @GetMapping("/api/offers/{offerPublicId}/reviews")
    public List<ReviewResponse> getOfferReviews(@PathVariable UUID offerPublicId) {
        return reviewService.getOfferReviews(offerPublicId);
    }
}
