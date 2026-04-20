package com.skillswap.market.offer.repository;

import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import org.springframework.data.jpa.domain.Specification;

public final class OfferSpecifications {

    private OfferSpecifications() {
    }

    public static Specification<SkillOffer> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), OfferStatus.ACTIVE);
    }

    public static Specification<SkillOffer> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<SkillOffer> titleOrDescriptionContains(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<SkillOffer> priceAtLeast(Integer minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceCredits"), minPrice);
    }

    public static Specification<SkillOffer> priceAtMost(Integer maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceCredits"), maxPrice);
    }

    public static Specification<SkillOffer> hasMentorId(Long mentorId) {
        return (root, query, cb) -> cb.equal(root.get("mentor").get("id"), mentorId);
    }
}
