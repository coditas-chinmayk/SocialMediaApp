package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.CreatePostRequest;
import com.example.SocialMedia.dto.PostDetailDto;
import com.example.SocialMedia.dto.PostDto;
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
    public ResponseEntity<PostDto> editFlaggedPost(@PathVariable Long postId, @RequestBody CreatePostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.ok(postService.editFlaggedPost(postId, user.getId(), request));
    }
}