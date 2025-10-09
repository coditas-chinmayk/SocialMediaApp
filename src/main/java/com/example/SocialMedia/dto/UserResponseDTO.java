package com.example.SocialMedia.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
}