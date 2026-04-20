package com.skillswap.market.booking.repository;

import com.skillswap.market.booking.entity.Booking;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"slot", "offer", "student", "mentor"})
    Optional<Booking> findOneById(Long id);

    @EntityGraph(attributePaths = {"slot", "offer", "student", "mentor"})
    Optional<Booking> findOneByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from Booking b
            join fetch b.slot
            join fetch b.offer
            join fetch b.student
            join fetch b.mentor
            left join fetch b.cancelledByUser
            where b.id = :bookingId
            """)
    Optional<Booking> findForUpdateById(@Param("bookingId") Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from Booking b
            join fetch b.slot
            join fetch b.offer
            join fetch b.student
            join fetch b.mentor
            left join fetch b.cancelledByUser
            where b.publicId = :publicId
            """)
    Optional<Booking> findForUpdateByPublicId(@Param("publicId") UUID publicId);

    @EntityGraph(attributePaths = {"slot", "offer", "student", "mentor"})
    List<Booking> findByStudentIdOrMentorIdOrderByCreatedAtDesc(Long studentId, Long mentorId);

    @EntityGraph(attributePaths = {"slot", "offer", "student", "mentor"})
    List<Booking> findAllByOrderByCreatedAtDesc();
}
