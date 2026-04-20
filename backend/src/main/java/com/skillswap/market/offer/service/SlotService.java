package com.skillswap.market.offer.service;

import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.dto.CreateSlotBatchRequest;
import com.skillswap.market.offer.dto.SlotResponse;
import com.skillswap.market.offer.entity.AvailabilitySlot;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.entity.SlotStatus;
import com.skillswap.market.offer.repository.AvailabilitySlotRepository;
import com.skillswap.market.offer.repository.SkillOfferRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final SkillOfferRepository skillOfferRepository;

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByOfferId(UUID offerPublicId) {
        SkillOffer offer = getOfferByPublicId(offerPublicId);
        return availabilitySlotRepository.findByOfferIdOrderByStartTimeAsc(offer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<SlotResponse> createSlots(AppUserPrincipal principal, UUID offerPublicId, CreateSlotBatchRequest request) {
        requireMentorOrAdmin(principal);
        SkillOffer offer = findOwnedOrAdminOffer(principal, offerPublicId);
        validateOfferForSlotCreation(offer);
        OffsetDateTime batchStart = toUtcOffsetDateTime(request.date(), request.startTime());
        OffsetDateTime batchEnd = toUtcOffsetDateTime(request.date(), request.endTime());
        validateBatchWindow(batchStart, batchEnd, offer.getDurationMinutes());

        List<AvailabilitySlot> slotsToCreate = new ArrayList<>();
        OffsetDateTime currentStart = batchStart;
        while (!currentStart.plusMinutes(offer.getDurationMinutes()).isAfter(batchEnd)) {
            OffsetDateTime currentEnd = currentStart.plusMinutes(offer.getDurationMinutes());
            ensureNoOverlap(offer.getMentor().getId(), currentStart, currentEnd);
            slotsToCreate.add(AvailabilitySlot.builder()
                    .offer(offer)
                    .startTime(currentStart)
                    .endTime(currentEnd)
                    .status(SlotStatus.OPEN)
                    .build());
            currentStart = currentEnd;
        }

        if (slotsToCreate.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "The selected time range does not fit one full slot");
        }

        return availabilitySlotRepository.saveAll(slotsToCreate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteSlot(AppUserPrincipal principal, Long slotId) {
        requireMentorOrAdmin(principal);
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Slot not found"));

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (!isAdmin && !slot.getOffer().getMentor().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own slots");
        }
        if (slot.getStatus() != SlotStatus.OPEN) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Only OPEN slots can be deleted");
        }
        if (!slot.getStartTime().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Only future slots can be deleted");
        }

        availabilitySlotRepository.delete(slot);
    }

    private void validateOfferForSlotCreation(SkillOffer offer) {
        if (offer.getStatus() != OfferStatus.ACTIVE) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Slots can only be created for ACTIVE offers");
        }
    }

    private SkillOffer findOwnedOrAdminOffer(AppUserPrincipal principal, UUID offerPublicId) {
        SkillOffer offer = getOfferByPublicId(offerPublicId);
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        if (!isAdmin && !offer.getMentor().getId().equals(principal.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage offers you own");
        }

        return offer;
    }

    private void validateBatchWindow(OffsetDateTime startTime, OffsetDateTime endTime, Integer durationMinutes) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (!startTime.isAfter(now)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Slot cannot be created in the past");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Slot end time must be after start time");
        }
        if (endTime.isBefore(startTime.plusMinutes(durationMinutes))) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "End time must allow at least one full slot");
        }
    }

    private OffsetDateTime toUtcOffsetDateTime(java.time.LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private void ensureNoOverlap(Long mentorId, OffsetDateTime startTime, OffsetDateTime endTime) {
        boolean overlapping = availabilitySlotRepository.existsOverlappingSlotForMentor(
                mentorId,
                startTime.withOffsetSameInstant(ZoneOffset.UTC),
                endTime.withOffsetSameInstant(ZoneOffset.UTC),
                List.of(SlotStatus.OPEN, SlotStatus.BOOKED)
        );
        if (overlapping) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Slot overlaps with another active slot");
        }
    }

    private SkillOffer getOfferByPublicId(UUID offerPublicId) {
        return skillOfferRepository.findByPublicId(offerPublicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offer not found"));
    }

    private void requireMentorOrAdmin(AppUserPrincipal principal) {
        if (!principal.roles().contains(Role.MENTOR) && !principal.roles().contains(Role.ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "MENTOR role is required for slot management");
        }
    }

    private SlotResponse toResponse(AvailabilitySlot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getOffer().getId(),
                slot.getOffer().getPublicId(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus(),
                slot.getCreatedAt()
        );
    }
}
