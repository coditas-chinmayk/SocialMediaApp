//package com.example.SocialMedia.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Data
//@Entity
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ModerationAction {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    private ModerationType type; // e.g., POST, COMMENT
//
//    private Long targetId; // ID of the post or comment
//
//    @Enumerated(EnumType.STRING)
//    private PostStatus previousStatus; // Or CommentStatus; could generalize to a string if needed
//
//    @Enumerated(EnumType.STRING)
//    private PostStatus newStatus;
//
//    private String reason; // Optional notes from moderator
//
//    private LocalDateTime actionAt = LocalDateTime.now();
//
//    @ManyToOne
//    @JoinColumn(name = "moderator_id", nullable = false)
//    private User moderator; // The moderator who took the action
//}
//
//public enum ModerationType {
//    POST, COMMENT;
//}