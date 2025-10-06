package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.Notification;
import com.example.SocialMedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}