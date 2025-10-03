package com.example.SocialMedia.dto;

import com.example.SocialMedia.entity.PostStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSummaryDto {
    private Long id;
    private String username;
}