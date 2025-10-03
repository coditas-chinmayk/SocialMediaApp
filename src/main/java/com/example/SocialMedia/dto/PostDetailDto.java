package com.example.SocialMedia.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostDetailDto extends PostDto {
    private List<CommentDto> comments;
}