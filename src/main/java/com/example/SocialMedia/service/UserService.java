package com.example.SocialMedia.service;

import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.Constants.ModerationType;
import com.example.SocialMedia.Constants.RequestStatus;
import com.example.SocialMedia.Constants.UserStatus;
import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.ModerationActionRepository;
import com.example.SocialMedia.repository.ModeratorRequestRepository;
import com.example.SocialMedia.repository.RoleRepository;
import com.example.SocialMedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private ModerationActionRepository moderationActionRepository;

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

    public List<UserListDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserListDto)
                .collect(Collectors.toList());
    }

    public List<UserListDto> getAllModerators() {
        List<User> moderators = userRepository.findAllModerators();
        return moderators.stream()
                .map(this::mapToUserListDto)
                .collect(Collectors.toList());
    }

    public List<UserListDto> getAllAdmins() {
        List<User> admins = userRepository.findAllAdmins();
        return admins.stream()
                .map(this::mapToUserListDto)
                .collect(Collectors.toList());
    }

    private UserListDto mapToUserListDto(User user) {
        UserListDto dto = new UserListDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));

        // Optional: Count posts and comments
        if (user.getPosts() != null) {
            dto.setPostCount((long) user.getPosts().size());
        }
        if (user.getComments() != null) {
            dto.setCommentCount((long) user.getComments().size());
        }

        return dto;
    }

    @Transactional
    public ModeratorRequest createModeratorRequest(Long userId) throws Throwable {
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("No user found by this id "));

        if (moderatorRequestRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Moderator request already exists for this user");
        }

        ModeratorRequest request = ModeratorRequest.builder()
                .requestStatus(RequestStatus.PENDING)
                .user(user)
                .build();

        return moderatorRequestRepository.save(request);
    }

    //AdminService layer below
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<ModeratorRequestDto> getModeratorRequests() {
        return moderatorRequestRepository.findAll().stream()
                .map(this::mapToModeratorRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ModeratorRequestDto approveModeratorRequest(Long requestId, String reason) {
        ModeratorRequest request = moderatorRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Moderator request not found"));

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setRequestStatus(RequestStatus.APPROVED);
        request.setUpdatedAt(LocalDateTime.now());

        // Assign MODERATOR role to user
        User user = request.getUser();
        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new NoSuchElementException("Moderator role not found"));
        user.getRoles().add(moderatorRole);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        moderatorRequestRepository.save(request);

        // TODO: Notify user (e.g., email)
        return mapToModeratorRequestDto(request);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ModeratorRequestDto denyModeratorRequest(Long requestId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for denial");
        }

        ModeratorRequest request = moderatorRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Moderator request not found"));

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setRequestStatus(RequestStatus.DENIED);
        request.setUpdatedAt(LocalDateTime.now());

        moderatorRequestRepository.save(request);

        // TODO: Notify user (e.g., email)
        return mapToModeratorRequestDto(request);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserResponseDTO revokeModerator(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new NoSuchElementException("Moderator role not found"));

        if (!user.getRoles().remove(moderatorRole)) {
            throw new IllegalStateException("User is not a moderator");
        }

        userRepository.save(user);

        // TODO: Notify user (e.g., email)
        return mapToUserResponseDTO(user);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public UserResponseDTO createAdmin(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new NoSuchElementException("Admin role not found"));
        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new NoSuchElementException("Moderator role not found"));

        Role authorRole = roleRepository.findByName("AUTHOR")
                .orElseThrow(()-> new NoSuchElementException("Author Role not found"));

        // Add ADMIN and MODERATOR roles
        user.getRoles().add(adminRole);
        user.getRoles().add(moderatorRole); // Admins can moderate
        user.getRoles().add(authorRole);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Log action (optional)
        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.ADMIN_CREATION)
                .targetId(userId)
                .previousStatus(null) // No status for users
                .newStatus(null)
                .reason(reason)
                .moderator(userRepository.findByUsername("superadmin")
                        .orElseThrow(() -> new NoSuchElementException("Super Admin not found")))
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return mapToUserResponseDTO(user);
    }


    //helper
    private UserSummaryDto mapToUserSummaryDto(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }

}