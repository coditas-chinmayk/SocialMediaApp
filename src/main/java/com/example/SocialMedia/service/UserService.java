package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.ModeratorRequestDto;
import com.example.SocialMedia.dto.PostDto;
import com.example.SocialMedia.dto.UserProfileDto;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.ModeratorRequestRepository;
import com.example.SocialMedia.repository.RoleRepository;
import com.example.SocialMedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModeratorRequestRepository moderatorRequestRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User signup(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        // Assigning default AUTHOR role
        Role userRole = roleRepository.findByName("AUTHOR")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return (User) userRepository.save(user);
    }

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());

        Map<ContentStatus, List<PostDto>> postsByStatus = new HashMap<>();
        for (ContentStatus status : ContentStatus.values()) {
            List<PostDto> posts = user.getPosts().stream()
                    .filter(post -> post.getPostStatus() == status)
                    .map(PostService::mapToPostDto)
                    .collect(Collectors.toList());
            postsByStatus.put(status, posts);
        }
        dto.setPostsByStatus(postsByStatus);

        return dto;
    }
    public ModeratorRequestDto requestModeratorRole(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("MODERATOR") || role.getName().equals("ADMIN"))) {
            throw new IllegalStateException("User is already a Moderator or Admin");
        }

        ModeratorRequest request = ModeratorRequest.builder()
                .user(user)
                .requestStatus(RequestStatus.PENDING)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();

        request = moderatorRequestRepository.save(request);
        return mapToModeratorRequestDto(request);
    }

    private ModeratorRequestDto mapToModeratorRequestDto(ModeratorRequest request) {
        ModeratorRequestDto dto = new ModeratorRequestDto();
        dto.setId(request.getId());
        dto.setRequestStatus(request.getRequestStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setUser(PostService.mapToUserSummaryDto(request.getUser()));
        return dto;
    }

}