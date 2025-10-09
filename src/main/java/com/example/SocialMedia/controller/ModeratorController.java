package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.PostDto;
import com.example.SocialMedia.dto.PostWithModeratorDto;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.AuthService;
import com.example.SocialMedia.service.ModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
public class ModeratorController {

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private AuthService authService;

    @GetMapping("/posts/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<PostDto>> getPendingPosts() {
        return ResponseEntity.ok(moderationService.getPendingPosts());
    }

    @PostMapping("/posts/{postId}/approve")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<PostWithModeratorDto> approvePost(
            @PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(moderationService.approvePost(postId, moderator.getId(), reason));
    }

    @PostMapping("/posts/{postId}/flag")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<PostWithModeratorDto> flagPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(moderationService.flagPost(postId, moderator.getId(), reason));
    }

    @PostMapping("/posts/{postId}/deny")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<PostWithModeratorDto> denyPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(moderationService.denyPost(postId, moderator.getId(), reason));
    }

    @GetMapping("/comments/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<CommentDto>> getPendingComments() {
        return ResponseEntity.ok(moderationService.getPendingComments());
    }

    @PostMapping("/comments/{commentId}/approve")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<CommentDto> approveComment(
            @PathVariable Long commentId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(moderationService.approveComment(commentId, moderator.getId(), reason));
    }

    @PostMapping("/comments/{commentId}/flag")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<CommentDto> flagComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(moderationService.flagComment(commentId, moderator.getId(), reason));
    }

    @PostMapping("/comments/{commentId}/deny")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<CommentDto> denyComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(moderationService.denyComment(commentId, moderator.getId(), reason));
    }
}