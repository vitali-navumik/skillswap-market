package com.skillswap.market.offer.controller;

import com.skillswap.market.offer.dto.CreateSlotBatchRequest;
import com.skillswap.market.offer.dto.SlotResponse;
import com.skillswap.market.offer.service.SlotService;
import com.skillswap.market.security.model.AppUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @PostMapping("/api/offers/{offerPublicId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SlotResponse> createSlot(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID offerPublicId,
            @Valid @RequestBody CreateSlotBatchRequest request
    ) {
        return slotService.createSlots(principal, offerPublicId, request);
    }

    @GetMapping("/api/offers/{offerPublicId}/slots")
    public List<SlotResponse> getSlots(@PathVariable UUID offerPublicId) {
        return slotService.getSlotsByOfferId(offerPublicId);
    }

    @DeleteMapping("/api/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlot(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long slotId
    ) {
        slotService.deleteSlot(principal, slotId);
    }
}
