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
import com.example.SocialMedia.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Lazy
    private ModeratorRequestRepository moderatorRequestRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ModerationActionRepository moderationActionRepository;

    @Autowired
    @Lazy
    private PostService postService;

    /**
     * Authenticates a user and generates a JWT token.
     */
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtil.generateToken(username);
    }

    /**
     * Retrieves a user by username.
     */
    public User getUserFromUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

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

    @Transactional
    public UserProfileDto getUserProfile(Long userId) {
        // Fetch user with roles and posts in one query to avoid N+1
        User user = userRepository.findById(userId) // Custom method with @EntityGraph({"roles", "posts"})
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Set<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        String finalRole = getHighestRole(roleIds);

        boolean requestStatus = moderatorRequestRepository.existsByUserIdAndRequestStatus(user.getId(), RequestStatus.PENDING);

        Map<ContentStatus, List<PostDto>> postsByStatus = Optional.ofNullable(user.getPosts())
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.groupingBy(
                        Post::getPostStatus,
                        Collectors.mapping(postService::mapToPostDto, Collectors.toList())
                ));


        return UserProfileDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(finalRole)
                .moderatorRequest(requestStatus)
                .postsByStatus(postsByStatus)
                .build();
    }

    private String getHighestRole(Set<Long> roleIds) {
        if (roleIds.contains(4L)) return "SUPER_ADMIN";
        if (roleIds.contains(3L)) return "ADMIN";
        if (roleIds.contains(2L)) return "MODERATOR";
        return "AUTHOR";
    }

    public UserResponseDTO getUserByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        Set<Long> roleIds = (user.getRoles() != null)
                ? user.getRoles().stream().map(Role::getId).collect(Collectors.toSet())
                : Set.of();
        String finalRole = getHighestRole(roleIds);
        userResponse.setRole(finalRole);
//        userResponse.setRole(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return userResponse;
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
                .toList());

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
    public void deleteUserById(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new NoSuchElementException("No user with this id"));
        userRepository.delete(user);
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
        Set<Long> roleIds = (user.getRoles() != null)
                ? user.getRoles().stream().map(Role::getId).collect(Collectors.toSet())
                : Set.of();
        String finalRole = getHighestRole(roleIds);
        dto.setRole(finalRole);
//        dto.setRole(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return dto;
    }

}