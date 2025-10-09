package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.AuthService;
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

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(@RequestBody @Valid UserSignupDto request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();
        UserSignupResponseDto userSignupResponseDto = new UserSignupResponseDto();
//        userSignupResponseDto.setUsername(username);
        userSignupResponseDto.setResponse(username + " is registered successfully");
        userService.signup(username, email, password);
        return ResponseEntity.status(201).body(userSignupResponseDto);
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