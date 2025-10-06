package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ModeratorRequestCreateDto;
import com.example.SocialMedia.dto.ModeratorRequestDto;
import com.example.SocialMedia.dto.UserProfileDto;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.AuthService;
import com.example.SocialMedia.service.UserService;
import jakarta.validation.constraints.Email;
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

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        @Email
        String email = request.get("email");
        String password = request.get("password");

        if (username == null || username.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Username, email, and password are required");
        }

        userService.signup(username, email, password);
        return ResponseEntity.status(201).body("User registered successfully");
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<UserProfileDto> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @PostMapping("/moderator-request")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ModeratorRequestDto> requestModeratorRole(@RequestBody ModeratorRequestCreateDto request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.status(201).body(userService.requestModeratorRole(user.getId(), request.getReason()));
    }
}