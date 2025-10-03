package com.example.SocialMedia.dto;

import com.example.SocialMedia.entity.CommentStatus;
import com.example.SocialMedia.entity.Post;
import com.example.SocialMedia.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private CommentStatus commentStatus;
    private LocalDateTime createdAt;
    private Post post;
    private UserSummaryDto author;
}