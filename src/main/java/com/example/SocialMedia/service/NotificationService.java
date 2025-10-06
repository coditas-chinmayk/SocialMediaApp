package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.NotificationDto;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.NotificationRepository;
import com.example.SocialMedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createNotification(Long userId, NotificationType type, Long targetId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .targetId(targetId)
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();

        notificationRepository.save(notification);

        // TODO: Optionally send email notification using Spring Mail
        // e.g., emailService.sendEmail(user.getEmail(), "Content Flagged", message);
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToNotificationDto)
                .collect(Collectors.toList());
    }

    private NotificationDto mapToNotificationDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setTargetId(notification.getTargetId());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(notification.isRead());
        return dto;
    }
}