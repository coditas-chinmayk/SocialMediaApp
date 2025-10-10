package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.PostService;
import com.example.SocialMedia.service.UserService;
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
    private UserService authService;

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<PostDto>> createPost(@RequestBody CreatePostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.status(201).body(new ApiResponseDto<>(true, "Post created successfully", postService.createPost(user.getId(), request)));
    }

    @GetMapping("/feed")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getHomeFeed() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Home feed retrieved successfully", postService.getHomeFeed()));
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<PostDetailDto>> getPostDetail(@PathVariable Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        PostDetailDto postDetail = postService.getPostDetail(postId);
        ApiResponseDto<PostDetailDto> response = new ApiResponseDto<>(true, "Post details retrieved successfully", postDetail);

        // Visibility control: Allow author to see FLAGGED/DENIED posts
        if (!postDetail.getAuthor().getId().equals(user.getId()) &&
                (postDetail.getPostStatus() == ContentStatus.FLAGGED || postDetail.getPostStatus() == ContentStatus.DENIED)) {
            return ResponseEntity.status(403).body(new ApiResponseDto<>(false, "Forbidden", null));
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<EditFlaggedPostDto>> editFlaggedPost(@PathVariable Long postId, @RequestBody CreatePostRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Flagged post edited successfully", postService.editFlaggedPost(postId, user.getId(), request)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getPostsByStatus(@PathVariable ContentStatus status) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Posts retrieved by status successfully", postService.getPostsByStatus(status)));
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getApprovedPosts() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Approved posts retrieved successfully", postService.getApprovedPosts()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getPendingPosts() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Pending posts retrieved successfully", postService.getPendingPosts()));
    }

    @GetMapping("/flagged")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getFlaggedPosts() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Flagged posts retrieved successfully", postService.getFlaggedPosts()));
    }

    @GetMapping("/denied")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<PostDto>>> getDeniedPosts() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Denied posts retrieved successfully", postService.getDeniedPosts()));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<ApiResponseDto<Void>> deletePost(@PathVariable Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getUserFromUsername(username);
        postService.deletePost(postId, user.getId());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Post deleted", null));
    }
}