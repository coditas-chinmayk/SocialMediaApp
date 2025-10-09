package com.example.SocialMedia.dto;

import com.example.SocialMedia.Constants.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String message;
    private NotificationType type;
    private Long targetId;
    private LocalDateTime createdAt;
    private boolean read;
}