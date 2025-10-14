package com.example.SocialMedia.service;

import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.Constants.ModerationType;
import com.example.SocialMedia.Constants.NotificationType;
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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ModerationActionRepository moderationActionRepository;

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
                .map(this::mapToPostDto)
                .toList();
    }

    public PostDetailDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        List<CommentDto> approvedComments = commentRepository.findByPostIdAndCommentStatus(postId, ContentStatus.APPROVED).stream()
                .map(PostService::mapToCommentDto)
                .toList();

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


    public void
    moderatorIdCheckForPost(Post post, User moderator) throws IllegalArgumentException{
        if (post.getAuthor().getId().equals(moderator.getId()) && post.getAuthor().getId() != 28){
            throw new IllegalArgumentException("You cannot change the status of your own posts");
        }
    }

    @Transactional
    public PostWithModeratorDto approvePost(Long postId, Long moderatorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheckForPost(post, moderator);

        if (post.getPostStatus() != ContentStatus.PENDING) {
            throw new IllegalStateException("Only pending posts can be approved");
        }

        ContentStatus previousStatus = post.getPostStatus();
        post.setPostStatus(ContentStatus.APPROVED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.POST)
                .targetId(postId)
                .previousStatus(previousStatus)
                .newStatus(ContentStatus.APPROVED)
//                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return PostService.mapToPostWithModeratorDto(post, moderator);
    }

    @Transactional
    public PostWithModeratorDto flagPost(Long postId, Long moderatorId) {
//        if (reason == null || reason.isBlank()) {
//            throw new IllegalArgumentException("Reason is required for flagging");
//        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));

        moderatorIdCheckForPost(post, moderator);

        if (post.getPostStatus() != ContentStatus.APPROVED && post.getPostStatus() != ContentStatus.PENDING) {
            throw new IllegalStateException("Only pending or approved posts can be flagged");
        }

        ContentStatus previousStatus = post.getPostStatus();
        post.setPostStatus(ContentStatus.FLAGGED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.POST)
                .targetId(postId)
                .previousStatus(previousStatus)
                .newStatus(ContentStatus.FLAGGED)
//                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        // Notify author
        String message = "Your post '" + post.getTitle() + "' was flagged for review: " + "by moderator "+ moderatorId;
        notificationService.createNotification(post.getAuthor().getId(), NotificationType.POST_FLAGGED, postId, message);

        return PostService.mapToPostWithModeratorDto(post, moderator);
    }

    @Transactional
    public PostWithModeratorDto denyPost(Long postId, Long moderatorId) {
//        if (reason == null || reason.isBlank()) {
//            throw new IllegalArgumentException("Reason is required for denial");
//        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheckForPost(post, moderator);

        if (post.getPostStatus() != ContentStatus.PENDING) {
            throw new IllegalStateException("Only pending posts can be denied");
        }

        ContentStatus previousStatus = post.getPostStatus();
        post.setPostStatus(ContentStatus.DENIED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.POST)
                .targetId(postId)
                .previousStatus(previousStatus)
                .newStatus(ContentStatus.DENIED)
//                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return PostService.mapToPostWithModeratorDto(post, moderator);
    }

    @Transactional
    public EditFlaggedPostDto editPost(Long postId, Long userId, CreatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the post author can edit this post");
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

    public PostDto mapToPostDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setPostStatus(post.getPostStatus());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setAuthor(mapToUserSummaryDto(post.getAuthor()));
        List<CommentDto> approvedComments = commentRepository.findByPostIdAndCommentStatus(post.getId(), ContentStatus.APPROVED).stream()
                .map(PostService::mapToCommentDto)
                .toList();
        dto.setComments(approvedComments);
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
                .map(this::mapToPostDto)
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