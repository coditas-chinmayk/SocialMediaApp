package com.example.SocialMedia.dto;

import com.example.SocialMedia.entity.ContentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class editFlaggedPostDto {
    private String title;
    private String content;
    private ContentStatus postStatus;
    private LocalDateTime updatedAt;


}
