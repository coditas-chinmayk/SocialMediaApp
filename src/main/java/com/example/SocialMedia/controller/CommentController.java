package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.CreateCommentRequest;
import com.example.SocialMedia.entity.ContentStatus;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.AuthService;
import com.example.SocialMedia.service.CommentService;
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
    private AuthService authService;

    @PostMapping("/posts/{postId}/comment")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        CommentDto commentDto = commentService.createComment(user.getId(), postId, request);
        return ResponseEntity.status(201).body(commentDto);
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<CommentDto> editFlaggedComment(@PathVariable Long commentId, @RequestBody CreateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        CommentDto commentDto = commentService.editFlaggedComment(commentId, user.getId(), request);
        return ResponseEntity.ok(commentDto);
    }


    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CommentDto>> getCommentsByStatus(@PathVariable ContentStatus status) {
        return ResponseEntity.ok(commentService.getCommentsByStatus(status));
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<List<CommentDto>> getApprovedComments() {
        return ResponseEntity.ok(commentService.getApprovedComments());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CommentDto>> getPendingComments() {
        return ResponseEntity.ok(commentService.getPendingComments());
    }

    @GetMapping("/flagged")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CommentDto>> getFlaggedComments() {
        return ResponseEntity.ok(commentService.getFlaggedComments());
    }

    @GetMapping("/denied")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CommentDto>> getDeniedComments() {
        return ResponseEntity.ok(commentService.getDeniedComments());
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}