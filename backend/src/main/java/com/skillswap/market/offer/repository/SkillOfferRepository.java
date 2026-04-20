package com.skillswap.market.offer.repository;

import com.skillswap.market.offer.entity.SkillOffer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SkillOfferRepository extends JpaRepository<SkillOffer, Long>, JpaSpecificationExecutor<SkillOffer> {

    Optional<SkillOffer> findByPublicId(UUID publicId);

    Optional<SkillOffer> findByMentorIdAndTitleIgnoreCase(Long mentorId, String title);

    Optional<SkillOffer> findByMentorIdAndIdempotencyKey(Long mentorId, String idempotencyKey);
}
