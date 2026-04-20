package com.skillswap.market.offer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.skillswap.market.user.entity.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @Mock
    private SkillOfferRepository skillOfferRepository;

    @InjectMocks
    private SlotService slotService;

    @Test
    void createSlotsCreatesOnlyFullSlots() {
        User mentor = mentor(10L);
        SkillOffer offer = offer(mentor, 60);
        UUID offerPublicId = offer.getPublicId();
        CreateSlotBatchRequest request = new CreateSlotBatchRequest(
                LocalDate.now(ZoneOffset.UTC).plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30)
        );

        when(skillOfferRepository.findByPublicId(offerPublicId)).thenReturn(java.util.Optional.of(offer));
        when(availabilitySlotRepository.existsOverlappingSlotForMentor(eq(mentor.getId()), any(), any(), anyList())).thenReturn(false);
        when(availabilitySlotRepository.saveAll(anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<AvailabilitySlot> slots = invocation.getArgument(0);
            long id = 100L;
            for (AvailabilitySlot slot : slots) {
                slot.setId(id++);
                slot.setCreatedAt(Instant.now());
            }
            return slots;
        });

        List<SlotResponse> created = slotService.createSlots(principal(mentor.getId(), Role.MENTOR), offerPublicId, request);

        assertThat(created).hasSize(1);
        OffsetDateTime expectedStart = LocalDateTime.of(request.date(), request.startTime())
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime();
        OffsetDateTime expectedEnd = expectedStart.plusMinutes(60);

        assertThat(created.get(0).startTime()).isEqualTo(expectedStart);
        assertThat(created.get(0).endTime()).isEqualTo(expectedEnd);
        verify(availabilitySlotRepository).saveAll(anyList());
    }

    @Test
    void createSlotsFailsWhenWindowDoesNotFitFullSlot() {
        User mentor = mentor(10L);
        SkillOffer offer = offer(mentor, 60);
        UUID offerPublicId = offer.getPublicId();
        CreateSlotBatchRequest request = new CreateSlotBatchRequest(
                LocalDate.now(ZoneOffset.UTC).plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        );

        when(skillOfferRepository.findByPublicId(offerPublicId)).thenReturn(java.util.Optional.of(offer));

        assertThatThrownBy(() -> slotService.createSlots(principal(mentor.getId(), Role.MENTOR), offerPublicId, request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(apiException.getMessage()).contains("End time must allow at least one full slot");
                });
    }

    private AppUserPrincipal principal(Long id, Role... roles) {
        Set<Role> roleSet = EnumSet.noneOf(Role.class);
        roleSet.addAll(List.of(roles));
        Set<GrantedAuthority> authorities = roleSet.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        return new AppUserPrincipal(id, "user@example.com", "encoded", true, roleSet, authorities);
    }

    private User mentor(Long id) {
        return User.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .email("mentor@example.com")
                .firstName("Mentor")
                .lastName("User")
                .displayName("Mentor User")
                .roles(EnumSet.of(Role.MENTOR))
                .build();
    }

    private SkillOffer offer(User mentor, int durationMinutes) {
        return SkillOffer.builder()
                .id(20L)
                .publicId(UUID.randomUUID())
                .mentor(mentor)
                .title("Offer")
                .description("desc")
                .category("Frontend")
                .durationMinutes(durationMinutes)
                .priceCredits(50)
                .cancellationPolicyHours(24)
                .status(OfferStatus.ACTIVE)
                .build();
    }
}
