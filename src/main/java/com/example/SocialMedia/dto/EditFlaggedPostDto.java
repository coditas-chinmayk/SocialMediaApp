package com.example.SocialMedia.dto;

import com.example.SocialMedia.Constants.ContentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EditFlaggedPostDto {
    private String title;
    private String content;
    private ContentStatus postStatus;
    private LocalDateTime updatedAt;


}
