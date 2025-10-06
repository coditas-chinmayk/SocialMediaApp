package com.example.SocialMedia.dto;

import com.example.SocialMedia.entity.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModeratorRequestDto {
    private Long id;
    private RequestStatus requestStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryDto user;
}