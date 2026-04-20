package com.skillswap.market.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillswap.market.booking.entity.Booking;
import com.skillswap.market.booking.entity.BookingStatus;
import com.skillswap.market.booking.repository.BookingRepository;
import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.repository.SkillOfferRepository;
import com.skillswap.market.review.dto.AdminCreateReviewRequest;
import com.skillswap.market.review.dto.CreateReviewRequest;
import com.skillswap.market.review.dto.ReviewResponse;
import com.skillswap.market.review.dto.UpdateReviewRequest;
import com.skillswap.market.review.entity.Review;
import com.skillswap.market.review.repository.ReviewRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.repository.UserRepository;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SkillOfferRepository skillOfferRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewAllowsCompletedBookingStudentOnly() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(30L, mentor, "SQL review target");
        Booking booking = booking(99L, offer, student, mentor, BookingStatus.COMPLETED);

        when(bookingRepository.findOneByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));
        when(reviewRepository.existsByBookingIdAndAuthorId(99L, 10L)).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(java.util.Optional.of(student));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(501L);
            review.setPublicId(UUID.randomUUID());
            return review;
        });

        ReviewResponse response = reviewService.createReview(
                principal(10L, Role.STUDENT),
                booking.getPublicId(),
                new CreateReviewRequest(5, "Very helpful mentor")
        );

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getOffer()).isEqualTo(offer);
        assertThat(captor.getValue().getTargetUser()).isEqualTo(mentor);
        assertThat(captor.getValue().isCreatedInAdminScope()).isFalse();
        assertThat(response.id()).isEqualTo(501L);
        assertThat(response.offerId()).isEqualTo(30L);
        assertThat(response.offerTitle()).isEqualTo("SQL review target");
        assertThat(response.bookingId()).isEqualTo(99L);
        assertThat(response.rating()).isEqualTo(5);
    }

    @Test
    void createReviewRejectsDuplicateReview() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(30L, mentor, "SQL");
        Booking booking = booking(100L, offer, student, mentor, BookingStatus.COMPLETED);

        when(bookingRepository.findOneByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));
        when(reviewRepository.existsByBookingIdAndAuthorId(100L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(
                principal(10L, Role.STUDENT),
                booking.getPublicId(),
                new CreateReviewRequest(4, "Good")
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createReviewRejectsNonStudentEvenIfBookingCompleted() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(30L, mentor, "SQL");
        Booking booking = booking(101L, offer, student, mentor, BookingStatus.COMPLETED);

        when(bookingRepository.findOneByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));

        assertThatThrownBy(() -> reviewService.createReview(
                principal(20L, Role.MENTOR),
                booking.getPublicId(),
                new CreateReviewRequest(4, "Mentor cannot review")
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void createReviewRejectsSelfReview() {
        User user = user(10L, Role.STUDENT);
        SkillOffer offer = offer(30L, user, "Self review demo offer");
        Booking booking = booking(102L, offer, user, user, BookingStatus.COMPLETED);

        when(bookingRepository.findOneByPublicId(booking.getPublicId())).thenReturn(java.util.Optional.of(booking));

        assertThatThrownBy(() -> reviewService.createReview(
                principal(10L, Role.STUDENT),
                booking.getPublicId(),
                new CreateReviewRequest(5, "Self review should be blocked")
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(apiException.getMessage()).contains("Self-review is not supported");
                });
    }

    @Test
    void updateReviewAllowsAuthorOnly() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(30L, mentor, "SQL");
        Review review = review(700L, offer, booking(90L, offer, student, mentor, BookingStatus.COMPLETED), student, mentor);

        when(reviewRepository.findOneByPublicId(review.getPublicId())).thenReturn(java.util.Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.updateReview(
                principal(10L, Role.STUDENT),
                review.getPublicId(),
                new UpdateReviewRequest(4, "Updated text")
        );

        assertThat(response.rating()).isEqualTo(4);
        assertThat(response.comment()).isEqualTo("Updated text");
    }

    @Test
    void deleteOwnReviewRejectsForeignAuthor() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        User foreignStudent = user(30L, Role.STUDENT);
        SkillOffer offer = offer(40L, mentor, "Career");
        Review review = review(701L, offer, booking(91L, offer, student, mentor, BookingStatus.COMPLETED), student, mentor);

        when(reviewRepository.findOneByPublicId(review.getPublicId())).thenReturn(java.util.Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(principal(foreignStudent.getId(), Role.STUDENT), review.getPublicId()))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminCanCreateReviewWithoutCompletedBookingAndWithoutBookingReference() {
        User admin = user(1L, Role.ADMIN);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(50L, mentor, "Admin-authored offer review");

        when(skillOfferRepository.findById(50L)).thenReturn(java.util.Optional.of(offer));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(admin));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(900L);
            review.setPublicId(UUID.randomUUID());
            return review;
        });

        ReviewResponse response = reviewService.createDirectReview(
                principal(1L, Role.ADMIN),
                new AdminCreateReviewRequest(50L, null, null, 5, "Admin editorial review")
        );

        assertThat(response.id()).isEqualTo(900L);
        assertThat(response.offerId()).isEqualTo(50L);
        assertThat(response.bookingId()).isNull();
        assertThat(response.createdInAdminScope()).isTrue();
        assertThat(response.targetUserId()).isEqualTo(20L);
    }

    @Test
    void adminCanUpdateAndDeleteAnyReview() {
        User admin = user(1L, Role.ADMIN);
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(60L, mentor, "Review moderation");
        Review review = review(800L, offer, booking(92L, offer, student, mentor, BookingStatus.COMPLETED), student, mentor);

        when(reviewRepository.findOneByPublicId(review.getPublicId())).thenReturn(java.util.Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.updateReview(
                principal(admin.getId(), Role.ADMIN),
                review.getPublicId(),
                new UpdateReviewRequest(2, "Admin edited")
        );
        reviewService.deleteReview(principal(admin.getId(), Role.ADMIN), review.getPublicId());

        assertThat(response.comment()).isEqualTo("Admin edited");
        verify(reviewRepository).delete(review);
    }

    @Test
    void getReviewsReturnsAuthorReviewsForRegularUser() {
        User student = user(10L, Role.STUDENT);
        User mentor = user(20L, Role.MENTOR);
        SkillOffer offer = offer(30L, mentor, "Excellent SQL");
        Review review = review(700L, offer, booking(90L, offer, student, mentor, BookingStatus.COMPLETED), student, mentor);

        when(reviewRepository.findByAuthorIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(review));

        List<ReviewResponse> response = reviewService.getReviews(principal(10L, Role.STUDENT));

        assertThat(response).hasSize(1);
        assertThat(response.get(0).bookingId()).isEqualTo(90L);
        assertThat(response.get(0).offerTitle()).isEqualTo("Excellent SQL");
    }

    private AppUserPrincipal principal(Long id, Role role) {
        Set<Role> roleSet = EnumSet.of(role);
        Set<GrantedAuthority> authorities = roleSet.stream()
                .map(currentRole -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + currentRole.name()))
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
                .build();
    }

    private SkillOffer offer(Long id, User mentor, String title) {
        return SkillOffer.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .mentor(mentor)
                .title(title)
                .description("Description")
                .category("QA")
                .durationMinutes(60)
                .priceCredits(50)
                .cancellationPolicyHours(24)
                .status(OfferStatus.ACTIVE)
                .build();
    }

    private Booking booking(Long id, SkillOffer offer, User student, User mentor, BookingStatus status) {
        return Booking.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .offer(offer)
                .student(student)
                .mentor(mentor)
                .status(status)
                .priceCredits(offer.getPriceCredits())
                .reservedAmount(offer.getPriceCredits())
                .build();
    }

    private Review review(Long id, SkillOffer offer, Booking booking, User author, User targetUser) {
        return Review.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .offer(offer)
                .booking(booking)
                .author(author)
                .targetUser(targetUser)
                .rating(5)
                .comment("Excellent session")
                .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2025-01-02T10:00:00Z"))
                .createdInAdminScope(false)
                .build();
    }
}
