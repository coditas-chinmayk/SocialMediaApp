package com.example.SocialMedia.service;

import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public PostDto createPost(Long userId, CreatePostRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank() || request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Title and content cannot be empty");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .postStatus(ContentStatus.PENDING)
                .author(author)
                .build();
        if (hasRole(author, "SUPER_ADMIN")) {
            post.setPostStatus(ContentStatus.APPROVED);
        }
        post = postRepository.save(post);
        return mapToPostDto(post);
    }
    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }

    public List<PostDto> getHomeFeed() {
        return postRepository.findByPostStatus(ContentStatus.APPROVED).stream()
                .map(PostService::mapToPostDto)
                .collect(Collectors.toList());
    }

    public PostDetailDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        List<CommentDto> approvedComments = commentRepository.findByPostIdAndCommentStatus(postId, ContentStatus.APPROVED).stream()
                .map(PostService::mapToCommentDto)
                .collect(Collectors.toList());

        PostDetailDto dto = new PostDetailDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPostStatus(post.getPostStatus());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setAuthor(mapToUserSummaryDto(post.getAuthor()));
        dto.setComments(approvedComments);
        return dto;
    }

    @Transactional
    public EditFlaggedPostDto editFlaggedPost(Long postId, Long userId, CreatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the post author can edit this post");
        }

        if (post.getPostStatus() != ContentStatus.FLAGGED) {
            throw new IllegalStateException("Only flagged posts can be edited");
        }

        if (request.getTitle() == null || request.getTitle().isBlank() || request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Title and content cannot be empty");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPostStatus(ContentStatus.PENDING);
        post.setUpdatedAt(LocalDateTime.now());

        post = postRepository.save(post);
        return mapToEditedPostDto(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // Only the post author can delete the post
        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the post author can delete this post");
        }
        // This will automatically delete all associated comments due to CascadeType.ALL
        postRepository.delete(post);
    }

    public static PostDto mapToPostDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPostStatus(post.getPostStatus());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setAuthor(mapToUserSummaryDto(post.getAuthor()));
        return dto;
    }

    public static PostWithModeratorDto mapToPostWithModeratorDto(Post post, User moderator) {
        PostWithModeratorDto dto = new PostWithModeratorDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setPostStatus(post.getPostStatus());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setAuthor(mapToUserSummaryDto(post.getAuthor()));
        dto.setModerator(mapToUserSummaryDto(moderator));
        return dto;
    }

    public static EditFlaggedPostDto mapToEditedPostDto(Post post) {
        EditFlaggedPostDto dto = new EditFlaggedPostDto();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPostStatus(post.getPostStatus());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCommentStatus(comment.getCommentStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setPostId(comment.getPost().getId());
        dto.setAuthor(mapToUserSummaryDto(comment.getAuthor()));
        return dto;
    }

    public static UserSummaryDto mapToUserSummaryDto(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    public List<PostDto> getPostsByStatus(ContentStatus status) {
        return postRepository.findByPostStatusOrderByCreatedAtDesc(status).stream()
                .map(PostService::mapToPostDto)
                .collect(Collectors.toList());
    }

    public List<PostDto> getApprovedPosts() {
        return getPostsByStatus(ContentStatus.APPROVED);
    }

    public List<PostDto> getPendingPosts() {
        return getPostsByStatus(ContentStatus.PENDING);
    }

    public List<PostDto> getFlaggedPosts() {
        return getPostsByStatus(ContentStatus.FLAGGED);
    }

    public List<PostDto> getDeniedPosts() {
        return getPostsByStatus(ContentStatus.DENIED);
    }
}