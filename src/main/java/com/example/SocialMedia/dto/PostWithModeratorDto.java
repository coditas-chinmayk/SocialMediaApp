package com.example.SocialMedia.dto;

import com.example.SocialMedia.Constants.ContentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostWithModeratorDto {
    private Long id;
    private String title;
    private ContentStatus postStatus;
    private LocalDateTime updatedAt;
    private UserSummaryDto author;
    private UserSummaryDto moderator;
}
