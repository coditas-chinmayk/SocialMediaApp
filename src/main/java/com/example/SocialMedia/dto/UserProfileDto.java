package com.example.SocialMedia.dto;

import com.example.SocialMedia.Constants.ContentStatus;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class UserProfileDto {
    private String username;
    private Map<ContentStatus, List<PostDto>>postsByStatus;
}