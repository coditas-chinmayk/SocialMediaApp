package com.example.SocialMedia.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserListDto {
    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private List<String> roles;

    private Long postCount;
    private Long commentCount;
}