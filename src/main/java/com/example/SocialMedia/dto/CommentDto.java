package com.example.SocialMedia.dto;

import com.example.SocialMedia.Constants.ContentStatus;
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
    private ContentStatus commentStatus;
    private LocalDateTime createdAt;
    private Long postId;
    private UserSummaryDto author;
}