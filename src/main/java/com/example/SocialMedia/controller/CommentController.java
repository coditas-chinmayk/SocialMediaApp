package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ApiResponseDto;
import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.CreateCommentRequest;
import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.CommentService;
import com.example.SocialMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService authService;

    @PostMapping("/posts/{postId}/comment")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<CommentDto>> createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        CommentDto commentDto = commentService.createComment(user.getId(), postId, request);
        return ResponseEntity.status(201).body(new ApiResponseDto<>(true, "comment created", commentDto));
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<CommentDto>> editFlaggedComment(@PathVariable Long commentId, @RequestBody CreateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        CommentDto commentDto = commentService.editFlaggedComment(commentId, user.getId(), request);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "flagged comment edited", commentDto));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<CommentDto>>> getCommentsByStatus(@PathVariable ContentStatus status) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "comment/s retrieved", commentService.getCommentsByStatus(status)));
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<List<CommentDto>>> getApprovedComments() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "approved comments retrieved", commentService.getApprovedComments()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<CommentDto>>> getPendingComments() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "pending comments retrieved", commentService.getPendingComments()));
    }

    @GetMapping("/flagged")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<CommentDto>>> getFlaggedComments() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "flagged comments retrieved", commentService.getFlaggedComments()));
    }

    @GetMapping("/denied")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<CommentDto>>> getDeniedComments() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "denied comments retrieved", commentService.getDeniedComments()));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(@PathVariable Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Comment deleted", null));
    }
}