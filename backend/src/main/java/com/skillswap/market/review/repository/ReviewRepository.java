package com.skillswap.market.review.repository;

import com.skillswap.market.review.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookingIdAndAuthorId(Long bookingId, Long authorId);

    @EntityGraph(attributePaths = {"author", "targetUser", "booking", "offer"})
    List<Review> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @EntityGraph(attributePaths = {"author", "targetUser", "offer"})
    List<Review> findByOfferIdOrderByCreatedAtDesc(Long offerId);

    @EntityGraph(attributePaths = {"author", "targetUser", "booking", "offer"})
    List<Review> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"author", "targetUser", "booking", "offer"})
    Optional<Review> findOneById(Long id);

    @EntityGraph(attributePaths = {"author", "targetUser", "booking", "offer"})
    Optional<Review> findOneByPublicId(UUID publicId);
}
