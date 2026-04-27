package com.skillswap.market.offer.controller;

import com.skillswap.market.common.api.PageResponse;
import com.skillswap.market.offer.dto.CreateOfferRequest;
import com.skillswap.market.offer.dto.OfferResponse;
import com.skillswap.market.offer.dto.UpdateOfferRequest;
import com.skillswap.market.offer.service.OfferService;
import com.skillswap.market.security.model.AppUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public PageResponse<OfferResponse> getOffers(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String search
    ) {
        return PageResponse.from(
                offerService.getOffers(principal, scope, page, size, sort, category, minPrice, maxPrice, search)
        );
    }

    @GetMapping("/{publicId}")
    public OfferResponse getOffer(@PathVariable UUID publicId) {
        return offerService.getOfferByPublicId(publicId);
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse createOffer(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOfferRequest request
    ) {
        return offerService.createOffer(principal, request, idempotencyKey);
    }

    @PostMapping("/update")
    public OfferResponse updateOffer(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody UpdateOfferRequest request
    ) {
        return offerService.updateOffer(principal, request);
    }
}
