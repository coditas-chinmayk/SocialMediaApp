package com.example.SocialMedia.dto;

import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.Constants.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
@Builder
public class UserProfileDto {
    private String username;
    private String email;
    private String role;
    private boolean moderatorRequest;
    private Map<ContentStatus, List<PostDto>>postsByStatus;

}