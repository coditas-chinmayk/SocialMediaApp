package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.ModeratorRequestDto;
import com.example.SocialMedia.dto.UserResponseDTO;
import com.example.SocialMedia.dto.UserSummaryDto;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.ModerationActionRepository;
import com.example.SocialMedia.repository.ModeratorRequestRepository;
import com.example.SocialMedia.repository.RoleRepository;
import com.example.SocialMedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private ModeratorRequestRepository moderatorRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModerationActionRepository moderationActionRepository;

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

        // Prevent Moderators from becoming Admins
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("MODERATOR") && !user.getRoles().contains(adminRole))) {
            throw new IllegalStateException("Moderators cannot be promoted to Admins");
        }

        // Add ADMIN and MODERATOR roles
        user.getRoles().add(adminRole);
        user.getRoles().add(moderatorRole); // Admins can moderate
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

    // Helper methods
    private ModeratorRequestDto mapToModeratorRequestDto(ModeratorRequest request) {
        ModeratorRequestDto dto = new ModeratorRequestDto();
        dto.setId(request.getId());
        dto.setRequestStatus(request.getRequestStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setUser(mapToUserSummaryDto(request.getUser()));
        return dto;
    }

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
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        return dto;
    }
}