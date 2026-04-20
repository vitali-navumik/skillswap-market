package com.skillswap.market.offer.repository;

import com.skillswap.market.offer.entity.AvailabilitySlot;
import com.skillswap.market.offer.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByOfferIdOrderByStartTimeAsc(Long offerId);

    @Query("""
            select s
            from AvailabilitySlot s
            join fetch s.offer o
            join fetch o.mentor
            where s.id = :slotId
            """)
    Optional<AvailabilitySlot> findWithOfferById(@Param("slotId") Long slotId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from AvailabilitySlot s
            join fetch s.offer o
            join fetch o.mentor
            where s.id = :slotId
            """)
    Optional<AvailabilitySlot> findWithOfferByIdForUpdate(@Param("slotId") Long slotId);

    @Query("""
            select case when count(s) > 0 then true else false end
            from AvailabilitySlot s
            join s.offer o
            where o.mentor.id = :mentorId
              and s.status in :statuses
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsOverlappingSlotForMentor(
            @Param("mentorId") Long mentorId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("statuses") List<SlotStatus> statuses
    );
}
