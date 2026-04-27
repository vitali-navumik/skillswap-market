package com.skillswap.market.user.controller;

import com.skillswap.market.security.model.AppUserPrincipal;
import com.skillswap.market.user.dto.CreateUserRequest;
import com.skillswap.market.user.dto.UpdateUserRequest;
import com.skillswap.market.user.dto.UserProfileResponse;
import com.skillswap.market.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse createUser(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.createUser(principal, request);
    }

    @GetMapping("/mentors")
    public List<UserProfileResponse> getMentors() {
        return userService.getMentors();
    }

    @GetMapping("/students")
    public List<UserProfileResponse> getStudents() {
        return userService.getStudents();
    }

    @GetMapping("/{publicId}")
    public UserProfileResponse getUser(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID publicId
    ) {
        return userService.getUserProfile(principal, publicId);
    }

    @PostMapping("/update")
    public UserProfileResponse updateUser(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(principal, request);
    }
}
