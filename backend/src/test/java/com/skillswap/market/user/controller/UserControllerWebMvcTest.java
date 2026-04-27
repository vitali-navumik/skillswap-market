package com.skillswap.market.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillswap.market.security.jwt.JwtAuthenticationFilter;
import com.skillswap.market.user.dto.UserProfileResponse;
import com.skillswap.market.user.entity.Role;
import com.skillswap.market.user.entity.UserStatus;
import com.skillswap.market.user.service.UserService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void updateUserAcceptsProfileUpdatePayload() throws Exception {
        UUID userPublicId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        UUID walletPublicId = UUID.fromString("22222222-2222-4222-8222-222222222222");
        org.mockito.Mockito.when(userService.updateUser(any(), any())).thenReturn(new UserProfileResponse(
                5L,
                userPublicId,
                walletPublicId,
                "user@example.com",
                "Updated",
                "Name",
                "Updated Name",
                Set.of(Role.STUDENT),
                UserStatus.ACTIVE,
                Instant.parse("2025-01-01T10:00:00Z"),
                Instant.parse("2025-01-03T10:00:00Z")
        ));

        mockMvc.perform(post("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "publicId": "11111111-1111-4111-8111-111111111111",
                                  "firstName": "Updated",
                                  "lastName": "Name",
                                  "displayName": "Updated Name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.displayName").value("Updated Name"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
