package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ApiResponseDto;
import com.example.SocialMedia.dto.ModeratorRequestDto;
import com.example.SocialMedia.dto.UserListDto;
import com.example.SocialMedia.dto.UserResponseDTO;
import com.example.SocialMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/moderator-requests")
    public ResponseEntity<ApiResponseDto<List<ModeratorRequestDto>>> getModeratorRequests() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Moderator requests retrieved successfully", userService.getModeratorRequests()));
    }

    @PostMapping("/moderator-requests/{requestId}/approve")
    public ResponseEntity<ApiResponseDto<ModeratorRequestDto>> approveModeratorRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Moderator request approved successfully", userService.approveModeratorRequest(requestId, reason)));
    }

    @PostMapping("/moderator-requests/{requestId}/deny")
    public ResponseEntity<ApiResponseDto<ModeratorRequestDto>> denyModeratorRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Moderator request denied successfully", userService.denyModeratorRequest(requestId, reason)));
    }

    @PostMapping("/moderators/{userId}/revoke")
    public ResponseEntity<ApiResponseDto<UserResponseDTO>> revokeModerator(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Moderator role revoked successfully", userService.revokeModerator(userId, reason)));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponseDto<UserResponseDTO>> createAdmin(
            @RequestBody Map<String, Long> body,
            @RequestBody(required = false) Map<String, String> reasonBody) {
        Long userId = body.get("userId");
        String reason = reasonBody != null ? reasonBody.get("reason") : null;
        return ResponseEntity.status(201).body(new ApiResponseDto<>(true, "Admin created successfully", userService.createAdmin(userId, reason)));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserListDto>>> getAllUsers() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Users retrieved successfully", userService.getAllUsers()));
    }

    @GetMapping("/moderators")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserListDto>>> getAllModerators() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Moderators retrieved successfully", userService.getAllModerators()));
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserListDto>>> getAllAdmins() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Admins retrieved successfully", userService.getAllAdmins()));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<UserResponseDTO>> getUserByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "User retrieved successfully", userService.getUserByUserId(userId)));
    }
//
//    @DeleteMapping("/users/delete/{userId}")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public ResponseEntity<ApiResponseDto<>>
}