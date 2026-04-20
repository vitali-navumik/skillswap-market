package com.skillswap.market.offer.service;

import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.dto.CreateOfferRequest;
import com.skillswap.market.offer.dto.OfferResponse;
import com.skillswap.market.offer.dto.UpdateOfferRequest;
import com.skillswap.market.offer.dto.UpdateOfferStatusRequest;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.repository.OfferSpecifications;
import com.skillswap.market.offer.repository.SkillOfferRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.repository.UserRepository;
import com.skillswap.market.user.support.UserNameFormatter;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final SkillOfferRepository skillOfferRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<OfferResponse> getPublicOffers(
            Integer page,
            Integer size,
            String sort,
            String category,
            Integer minPrice,
            Integer maxPrice,
            String search
    ) {
        return getOffers(null, null, page, size, sort, category, minPrice, maxPrice, search);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> getOffers(
            AppUserPrincipal principal,
            String scope,
            Integer page,
            Integer size,
            String sort,
            String category,
            Integer minPrice,
            Integer maxPrice,
            String search
    ) {
        if (Objects.equals(scope, "all")) {
            requireAdmin(principal);
            return skillOfferRepository.findAll(PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createdAt")))
                    .map(this::toResponse);
        }

        if (Objects.equals(scope, "mine")) {
            requireMentorOrAdmin(principal, "MENTOR role is required to view own offers");
            return skillOfferRepository.findAll(
                            OfferSpecifications.hasMentorId(principal.id()),
                            PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createdAt"))
                    )
                    .map(this::toResponse);
        }

        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 20 : Math.min(size, 100),
                resolveSort(sort)
        );

        Specification<SkillOffer> specification = Specification.where(OfferSpecifications.isActive());
        if (category != null && !category.isBlank()) {
            specification = specification.and(OfferSpecifications.hasCategory(category.trim()));
        }
        if (minPrice != null) {
            specification = specification.and(OfferSpecifications.priceAtLeast(minPrice));
        }
        if (maxPrice != null) {
            specification = specification.and(OfferSpecifications.priceAtMost(maxPrice));
        }
        if (search != null && !search.isBlank()) {
            specification = specification.and(OfferSpecifications.titleOrDescriptionContains(search.trim()));
        }

        return skillOfferRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OfferResponse getOfferByPublicId(UUID publicId) {
        SkillOffer offer = skillOfferRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offer not found"));
        return toResponse(offer);
    }

    @Transactional
    public OfferResponse createOffer(AppUserPrincipal principal, CreateOfferRequest request) {
        requireRole(principal, Role.MENTOR, "MENTOR role is required to create an offer");
        User mentor = getUser(principal.id());

        SkillOffer offer = SkillOffer.builder()
                .mentor(mentor)
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category().trim())
                .durationMinutes(request.durationMinutes())
                .priceCredits(request.priceCredits())
                .cancellationPolicyHours(24)
                .status(request.status() == null ? OfferStatus.DRAFT : request.status())
                .build();

        return toResponse(skillOfferRepository.save(offer));
    }

    @Transactional
    public OfferResponse updateOffer(AppUserPrincipal principal, UUID publicId, UpdateOfferRequest request) {
        requireMentorOrAdmin(principal, "MENTOR role is required to update an offer");
        SkillOffer offer = findOwnedOrAdminOffer(principal, publicId);

        if (request.title() != null && !request.title().isBlank()) {
            offer.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            offer.setDescription(request.description().trim());
        }
        if (request.category() != null && !request.category().isBlank()) {
            offer.setCategory(request.category().trim());
        }
        if (request.durationMinutes() != null) {
            offer.setDurationMinutes(request.durationMinutes());
        }
        if (request.priceCredits() != null) {
            offer.setPriceCredits(request.priceCredits());
        }
        if (request.status() != null) {
            if (!principal.roles().contains(Role.ADMIN)) {
                validateOwnerStatusTransition(offer.getStatus(), request.status());
            }
            offer.setStatus(request.status());
        }

        return toResponse(skillOfferRepository.save(offer));
    }

    @Transactional
    public OfferResponse updateOfferStatus(AppUserPrincipal principal, UUID publicId, UpdateOfferStatusRequest request) {
        SkillOffer offer = findOwnedOrAdminOffer(principal, publicId);
        OfferStatus targetStatus = request.status();
        if (!principal.roles().contains(Role.ADMIN)) {
            requireRole(principal, Role.MENTOR, "MENTOR role is required to change offer status");
            validateOwnerStatusTransition(offer.getStatus(), targetStatus);
        }

        offer.setStatus(targetStatus);
        return toResponse(skillOfferRepository.save(offer));
    }

    private SkillOffer findOwnedOrAdminOffer(AppUserPrincipal principal, UUID publicId) {
        SkillOffer offer = skillOfferRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offer not found"));

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (!isAdmin && !offer.getMentor().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own offers");
        }

        return offer;
    }

    private void validateOwnerStatusTransition(OfferStatus currentStatus, OfferStatus targetStatus) {
        boolean allowed = switch (currentStatus) {
            case DRAFT -> targetStatus == OfferStatus.ACTIVE;
            case ACTIVE -> targetStatus == OfferStatus.DRAFT || targetStatus == OfferStatus.ARCHIVED;
            case ARCHIVED -> false;
            case BLOCKED -> false;
        };

        if (!allowed) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported offer status transition");
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void requireRole(AppUserPrincipal principal, Role requiredRole, String message) {
        if (!principal.roles().contains(requiredRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void requireMentorOrAdmin(AppUserPrincipal principal, String message) {
        if (!principal.roles().contains(Role.MENTOR) && !principal.roles().contains(Role.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void requireAdmin(AppUserPrincipal principal) {
        if (principal == null || !principal.roles().contains(Role.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN role is required for this offer scope");
        }
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "priceCredits");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "priceCredits");
            case "titleAsc" -> Sort.by(Sort.Direction.ASC, "title");
            case "titleDesc" -> Sort.by(Sort.Direction.DESC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private OfferResponse toResponse(SkillOffer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getPublicId(),
                offer.getMentor().getId(),
                offer.getMentor().getPublicId(),
                UserNameFormatter.format(offer.getMentor()),
                offer.getTitle(),
                offer.getDescription(),
                offer.getCategory(),
                offer.getDurationMinutes(),
                offer.getPriceCredits(),
                offer.getCancellationPolicyHours(),
                offer.getStatus(),
                offer.getCreatedAt(),
                offer.getUpdatedAt()
        );
    }
}
