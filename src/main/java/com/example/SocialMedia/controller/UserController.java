package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<UserSignupResponseDto>> signup(@RequestBody @Valid UserSignupDto request) {
        String username = request.getUsername();
        String email = request.getEmail().toLowerCase();
        String password = request.getPassword();
        UserSignupResponseDto userSignupResponseDto = new UserSignupResponseDto();
        userSignupResponseDto.setResponse("Hey! " + username + " you are registered successfully");
        userService.signup(username, email, password);
        return ResponseEntity.status(201).body(new ApiResponseDto<>(true, "User registered successfully", userSignupResponseDto));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<UserProfileDto>> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserFromUsername(username);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "User profile retrieved successfully", userService.getUserProfile(user.getId())));
    }

    @PostMapping("/moderator-request")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<ModeratorRequestDto>> requestModeratorRole(@RequestBody ModeratorRequestCreateDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserFromUsername(username);
        return ResponseEntity.status(201).body(new ApiResponseDto<>(true, "Moderator role request submitted successfully", userService.requestModeratorRole(user.getId(), request.getReason())));
    }
}