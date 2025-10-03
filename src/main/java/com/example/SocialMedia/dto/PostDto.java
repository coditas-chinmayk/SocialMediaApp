package com.example.SocialMedia.dto;


import com.example.SocialMedia.dto.UserSummaryDto;
import com.example.SocialMedia.entity.PostStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private PostStatus postStatus;
    private LocalDateTime createdAt;
    private UserSummaryDto author;
}