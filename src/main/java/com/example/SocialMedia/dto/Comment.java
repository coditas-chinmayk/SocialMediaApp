package com.example.SocialMedia.dto;

import com.example.SocialMedia.entity.CommentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private String content;
    private CommentStatus commentStatus;
    private LocalDateTime createdAt;
    private UserSummaryDto author;
}