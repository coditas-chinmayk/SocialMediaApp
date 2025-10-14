package com.example.SocialMedia.dto;


import com.example.SocialMedia.Constants.ContentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private ContentStatus postStatus;
    private LocalDateTime createdAt;
    private UserSummaryDto author;
    private List<CommentDto> comments;
}