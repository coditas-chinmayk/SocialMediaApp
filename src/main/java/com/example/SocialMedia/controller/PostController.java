package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.CreatePostRequest;
import com.example.SocialMedia.dto.PostDetailDto;
import com.example.SocialMedia.dto.PostDto;
import com.example.SocialMedia.dto.editFlaggedPostDto;
import com.example.SocialMedia.entity.ContentStatus;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.AuthService;
import com.example.SocialMedia.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AuthService authService;

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.status(201).body(postService.createPost(user.getId(), request));
    }

    @GetMapping("/feed")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<List<PostDto>> getHomeFeed() {
        return ResponseEntity.ok(postService.getHomeFeed());
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<PostDetailDto> getPostDetail(@PathVariable Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        PostDetailDto postDetail = postService.getPostDetail(postId);

        // Visibility control: Allow author to see FLAGGED/DENIED posts
        if (!postDetail.getAuthor().getId().equals(user.getId()) &&
                (postDetail.getPostStatus() == ContentStatus.FLAGGED || postDetail.getPostStatus() == ContentStatus.DENIED)) {
            return ResponseEntity.status(403).body(null);
        }
        return ResponseEntity.ok(postDetail);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<editFlaggedPostDto> editFlaggedPost(@PathVariable Long postId, @RequestBody CreatePostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.ok(postService.editFlaggedPost(postId, user.getId(), request));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PostDto>> getPostsByStatus(@PathVariable ContentStatus status) {
        return ResponseEntity.ok(postService.getPostsByStatus(status));
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<List<PostDto>> getApprovedPosts() {
        return ResponseEntity.ok(postService.getApprovedPosts());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PostDto>> getPendingPosts() {
        return ResponseEntity.ok(postService.getPendingPosts());
    }

    @GetMapping("/flagged")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PostDto>> getFlaggedPosts() {
        return ResponseEntity.ok(postService.getFlaggedPosts());
    }

    @GetMapping("/denied")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PostDto>> getDeniedPosts() {
        return ResponseEntity.ok(postService.getDeniedPosts());
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        postService.deletePost(postId, user.getId());
        return ResponseEntity.noContent().build();
    }
}