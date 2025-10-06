package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ModeratorRequestDto;
import com.example.SocialMedia.dto.UserResponseDTO;
import com.example.SocialMedia.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

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
}