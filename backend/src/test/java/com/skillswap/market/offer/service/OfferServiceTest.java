package com.skillswap.market.offer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillswap.market.common.exception.ApiException;
import com.skillswap.market.offer.dto.CreateOfferRequest;
import com.skillswap.market.offer.dto.OfferResponse;
import com.skillswap.market.offer.dto.UpdateOfferRequest;
import com.skillswap.market.offer.dto.UpdateOfferStatusRequest;
import com.skillswap.market.offer.entity.OfferStatus;
import com.skillswap.market.offer.entity.SkillOffer;
import com.skillswap.market.offer.repository.SkillOfferRepository;
import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.User;
import com.skillswap.market.user.repository.UserRepository;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private SkillOfferRepository skillOfferRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OfferService offerService;

    @Test
    void createOfferRequiresMentorRole() {
        AppUserPrincipal studentPrincipal = principal(10L, Role.STUDENT);

        assertThatThrownBy(() -> offerService.createOffer(studentPrincipal, new CreateOfferRequest(
                "SQL mentoring",
                "Learn joins",
                "Databases",
                60,
                50,
                OfferStatus.DRAFT
        )))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminCannotCreateMarketplaceOffer() {
        AppUserPrincipal adminPrincipal = principal(1L, Role.ADMIN);

        assertThatThrownBy(() -> offerService.createOffer(adminPrincipal, new CreateOfferRequest(
                "Admin offer",
                "Admin should not become a marketplace mentor",
                "Operations",
                60,
                50,
                OfferStatus.DRAFT
        )))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void ownerCanPublishDraftOffer() {
        AppUserPrincipal mentorPrincipal = principal(11L, Role.MENTOR);
        UUID offerPublicId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        SkillOffer offer = SkillOffer.builder()
                .id(100L)
                .publicId(offerPublicId)
                .mentor(user(11L, "mentor@example.com"))
                .title("Draft")
                .description("desc")
                .category("Dev")
                .durationMinutes(60)
                .priceCredits(90)
                .cancellationPolicyHours(24)
                .status(OfferStatus.DRAFT)
                .build();

        when(skillOfferRepository.findByPublicId(offerPublicId)).thenReturn(java.util.Optional.of(offer));
        when(skillOfferRepository.save(any(SkillOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferResponse response = offerService.updateOfferStatus(mentorPrincipal, offerPublicId, new UpdateOfferStatusRequest(OfferStatus.ACTIVE));

        assertThat(response.status()).isEqualTo(OfferStatus.ACTIVE);
        verify(skillOfferRepository).save(offer);
    }

    @Test
    void adminCanUpdateOfferAndSetAnyStatus() {
        AppUserPrincipal adminPrincipal = principal(1L, Role.ADMIN);
        UUID offerPublicId = UUID.fromString("22222222-2222-4222-8222-222222222222");
        SkillOffer offer = SkillOffer.builder()
                .id(101L)
                .publicId(offerPublicId)
                .mentor(user(11L, "mentor@example.com"))
                .title("Java mentoring")
                .description("desc")
                .category("Dev")
                .durationMinutes(60)
                .priceCredits(120)
                .cancellationPolicyHours(24)
                .status(OfferStatus.ACTIVE)
                .build();

        when(skillOfferRepository.findByPublicId(offerPublicId)).thenReturn(java.util.Optional.of(offer));
        when(skillOfferRepository.save(any(SkillOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferResponse updated = offerService.updateOffer(adminPrincipal, offerPublicId, new UpdateOfferRequest(
                "Admin edited title",
                "Admin edited description",
                "Career",
                45,
                150,
                OfferStatus.BLOCKED
        ));

        assertThat(updated.title()).isEqualTo("Admin edited title");
        assertThat(updated.category()).isEqualTo("Career");
        assertThat(updated.status()).isEqualTo(OfferStatus.BLOCKED);

        ArgumentCaptor<SkillOffer> captor = ArgumentCaptor.forClass(SkillOffer.class);
        verify(skillOfferRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OfferStatus.BLOCKED);
    }

    @Test
    void getPublicOffersDefaultsToNewestSorting() {
        when(skillOfferRepository.findAll(anySpecification(), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));

        offerService.getPublicOffers(null, null, null, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(skillOfferRepository).findAll(anySpecification(), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
    }

    @Test
    void getPublicOffersAcceptsExplicitSortOption() {
        when(skillOfferRepository.findAll(anySpecification(), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));

        offerService.getPublicOffers(0, 20, "priceAsc", "Databases", null, null, "SQL");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(skillOfferRepository).findAll(anySpecification(), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort().getOrderFor("priceCredits")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("priceCredits").isAscending()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private Specification<SkillOffer> anySpecification() {
        return any(Specification.class);
    }

    private AppUserPrincipal principal(Long id, Role... roles) {
        Set<Role> roleSet = EnumSet.noneOf(Role.class);
        roleSet.addAll(java.util.List.of(roles));
        Set<GrantedAuthority> authorities = roleSet.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        return new AppUserPrincipal(
                id,
                "user@example.com",
                "encoded",
                true,
                roleSet,
                authorities
        );
    }

    private User user(Long id, String email) {
        return User.builder()
                .id(id)
                .publicId(UUID.randomUUID())
                .email(email)
                .firstName("Test")
                .lastName("User")
                .displayName("Test User")
                .roles(EnumSet.of(Role.MENTOR))
                .build();
    }
}
