package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ModeratorRequestDto;
import com.example.SocialMedia.dto.UserListDto;
import com.example.SocialMedia.dto.UserResponseDTO;
import com.example.SocialMedia.service.AdminService;
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
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @GetMapping("/moderator-requests")
    public ResponseEntity<List<ModeratorRequestDto>> getModeratorRequests() {
        return ResponseEntity.ok(adminService.getModeratorRequests());
    }

    @PostMapping("/moderator-requests/{requestId}/approve")
    public ResponseEntity<ModeratorRequestDto> approveModeratorRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.approveModeratorRequest(requestId, reason));
    }

    @PostMapping("/moderator-requests/{requestId}/deny")
    public ResponseEntity<ModeratorRequestDto> denyModeratorRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(adminService.denyModeratorRequest(requestId, reason));
    }

    @PostMapping("/moderators/{userId}/revoke")
    public ResponseEntity<UserResponseDTO> revokeModerator(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(adminService.revokeModerator(userId, reason));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createAdmin(
            @RequestBody Map<String, Long> body,
            @RequestBody(required = false) Map<String, String> reasonBody) {
        Long userId = body.get("userId");
        String reason = reasonBody != null ? reasonBody.get("reason") : null;
        return ResponseEntity.status(201).body(adminService.createAdmin(userId, reason));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserListDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/moderators")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserListDto>> getAllModerators() {
        return ResponseEntity.ok(userService.getAllModerators());
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserListDto>> getAllAdmins() {
        return ResponseEntity.ok(userService.getAllAdmins());
    }
}