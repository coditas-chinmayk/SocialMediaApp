package com.example.SocialMedia.dto;

import com.example.SocialMedia.entity.ContentStatus;
import com.example.SocialMedia.entity.PostStatus;
import com.example.SocialMedia.dto.PostDto;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class UserProfileDto {
    private String username;
    private Map<ContentStatus, List<PostDto>>postsByStatus;
}