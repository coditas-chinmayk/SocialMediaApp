package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ApiResponseDto;
import com.example.SocialMedia.dto.NotificationDto;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.NotificationService;
import com.example.SocialMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService authService;

    @GetMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<List<NotificationDto>>> getNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Notifications retrieved successfully", notificationService.getUserNotifications(user.getId())));
    }
}