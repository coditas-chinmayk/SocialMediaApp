package com.example.SocialMedia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Long targetId; // ID of post or comment

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "`read`")
    private boolean read = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}