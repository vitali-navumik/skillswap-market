package com.skillswap.market.review.service;

import com.skillswap.market.booking.entity.Booking;
import com.skillswap.market.booking.entity.BookingStatus;
import com.skillswap.market.booking.repository.BookingRepository;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.repository.SkillOfferRepository;
import com.skillswap.market.review.dto.AdminCreateReviewRequest;
import com.skillswap.market.review.dto.CreateReviewRequest;
import com.skillswap.market.review.dto.ReviewResponse;
import com.skillswap.market.review.dto.UpdateReviewRequest;
import com.skillswap.market.review.entity.Review;
import com.skillswap.market.review.repository.ReviewRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.user.support.UserNameFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(AppUserPrincipal principal, UUID bookingPublicId, CreateReviewRequest request) {
        Booking booking = bookingRepository.findOneByPublicId(bookingPublicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Review can only be created after COMPLETED");
        }
        if (!booking.getStudent().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the booking student can leave a review in MVP");
        }
        if (booking.getStudent().getId().equals(booking.getMentor().getId())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Self-review is not supported in MVP");
        }
        if (reviewRepository.existsByBookingIdAndAuthorId(booking.getId(), principal.id())) {
            throw new ApiException(HttpStatus.CONFLICT, "Review already exists for this booking and author");
        }
        User author = userRepository.findById(principal.id())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = Review.builder()
                .offer(booking.getOffer())
                .booking(booking)
                .author(author)
                .targetUser(booking.getMentor())
                .rating(request.rating())
                .comment(request.comment().trim())
                .createdInAdminScope(false)
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getOfferReviews(UUID offerPublicId) {
        SkillOffer offer = skillOfferRepository.findByPublicId(offerPublicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offer not found"));
        return reviewRepository.findByOfferIdOrderByCreatedAtDesc(offer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(AppUserPrincipal principal) {
        if (principal.roles().contains(Role.ADMIN)) {
            return reviewRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return reviewRepository.findByAuthorIdOrderByCreatedAtDesc(principal.id())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(AppUserPrincipal principal, UUID reviewPublicId) {
        Review review = getReviewEntity(reviewPublicId);
        assertReviewAccessible(principal, review);
        return toResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(AppUserPrincipal principal, UUID reviewPublicId, UpdateReviewRequest request) {
        Review review = getReviewEntity(reviewPublicId);
        assertReviewMutable(principal, review);
        applyReviewUpdate(review, request);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(AppUserPrincipal principal, UUID reviewPublicId) {
        Review review = getReviewEntity(reviewPublicId);
        assertReviewMutable(principal, review);
        reviewRepository.delete(review);
    }

    @Transactional
    public ReviewResponse createDirectReview(AppUserPrincipal principal, AdminCreateReviewRequest request) {
        User author = getUser(principal.id());
        SkillOffer offer = skillOfferRepository.findById(request.offerId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offer not found"));
        Booking booking = request.bookingId() == null ? null : bookingRepository.findOneById(request.bookingId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking != null && !booking.getOffer().getId().equals(offer.getId())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Booking does not belong to the specified offer");
        }

        User targetUser = request.targetUserId() == null
                ? offer.getMentor()
                : userRepository.findById(request.targetUserId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target user not found"));

        Review review = Review.builder()
                .offer(offer)
                .booking(booking)
                .author(author)
                .targetUser(targetUser)
                .rating(request.rating())
                .comment(request.comment().trim())
                .createdInAdminScope(true)
                .build();

        return toResponse(reviewRepository.save(review));
    }

    private Review getReviewEntity(UUID reviewPublicId) {
        return reviewRepository.findOneByPublicId(reviewPublicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void applyReviewUpdate(Review review, UpdateReviewRequest request) {
        review.setRating(request.rating());
        review.setComment(request.comment().trim());
    }

    private void assertReviewAccessible(AppUserPrincipal principal, Review review) {
        if (principal.roles().contains(Role.ADMIN)) {
            return;
        }

        boolean isAuthor = review.getAuthor().getId().equals(principal.id());
        boolean isTargetUser = review.getTargetUser().getId().equals(principal.id());

        if (!isAuthor && !isTargetUser) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You may access only reviews related to you");
        }
    }

    private void assertReviewMutable(AppUserPrincipal principal, Review review) {
        if (!principal.roles().contains(Role.ADMIN) && !review.getAuthor().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You may mutate only your own review");
        }
    }

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getPublicId(),
                review.getOffer().getId(),
                review.getOffer().getPublicId(),
                review.getOffer().getTitle(),
                review.getBooking() == null ? null : review.getBooking().getId(),
                review.getBooking() == null ? null : review.getBooking().getPublicId(),
                review.getAuthor().getId(),
                review.getAuthor().getPublicId(),
                UserNameFormatter.format(review.getAuthor()),
                review.getTargetUser().getId(),
                review.getTargetUser().getPublicId(),
                UserNameFormatter.format(review.getTargetUser()),
                review.getRating(),
                review.getComment(),
                review.isCreatedInAdminScope(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
