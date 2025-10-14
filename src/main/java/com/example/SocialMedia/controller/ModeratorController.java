package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ApiResponseDto;
import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.PostDto;
import com.example.SocialMedia.dto.PostWithModeratorDto;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.CommentService;
import com.example.SocialMedia.service.PostService;
import com.example.SocialMedia.service.UserService;
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
    private PostService postService;

    @Autowired
    private UserService authService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/posts/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getPendingPosts() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Pending posts retrieved successfully", postService.getPendingPosts()));
    }

    @PostMapping("/posts/{postId}/approve")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<PostWithModeratorDto>> approvePost(
            @PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
//        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Post approved successfully", postService.approvePost(postId, moderator.getId())));
    }

    @PostMapping("/posts/{postId}/flag")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<PostWithModeratorDto>> flagPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Post flagged successfully", postService.flagPost(postId, moderator.getId())));
    }

    @PostMapping("/posts/{postId}/deny")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<PostWithModeratorDto>> denyPost(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Post denied successfully", postService.denyPost(postId, moderator.getId())));
    }

    @GetMapping("/comments/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<List<CommentDto>>> getPendingComments() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Pending comments retrieved successfully", commentService.getPendingComments()));
    }

    @PostMapping("/comments/{commentId}/approve")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<CommentDto>> approveComment(
            @PathVariable Long commentId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Comment approved successfully", commentService.approveComment(commentId, moderator.getId(), reason)));
    }

    @PostMapping("/comments/{commentId}/flag")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<CommentDto>> flagComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Comment flagged successfully", commentService.flagComment(commentId, moderator.getId(), reason)));
    }

    @PostMapping("/comments/{commentId}/deny")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ApiResponseDto<CommentDto>> denyComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User moderator = authService.getUserFromUsername(username);
        String reason = body.get("reason");
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Comment denied successfully", commentService.denyComment(commentId, moderator.getId(), reason)));
    }
}