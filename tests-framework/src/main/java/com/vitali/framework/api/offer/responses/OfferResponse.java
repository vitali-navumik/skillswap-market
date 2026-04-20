package com.vitali.framework.api.offer.responses;

import com.vitali.framework.enums.OfferStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class OfferResponse {
    private Long id;
    private UUID publicId;
    private Long mentorId;
    private UUID mentorPublicId;
    private String mentorDisplayName;
    private String title;
    private String description;
    private String category;
    private Integer durationMinutes;
    private Integer priceCredits;
    private Integer cancellationPolicyHours;
    private OfferStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
