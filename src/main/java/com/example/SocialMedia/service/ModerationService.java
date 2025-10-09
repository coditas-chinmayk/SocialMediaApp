package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.PostDto;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.CommentRepository;
import com.example.SocialMedia.repository.ModerationActionRepository;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ModerationService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModerationActionRepository moderationActionRepository;

    @Autowired
    private NotificationService notificationService;


    public List<PostDto> getPendingPosts() {
        return postRepository.findByPostStatus(ContentStatus.PENDING).stream()
                .map(PostService::mapToPostDto)
                .collect(Collectors.toList());
    }
    public void moderatorIdCheck(Post post, User moderator) throws IllegalArgumentException{
        if (post.getAuthor().getId().equals(moderator.getId())) {
            throw new IllegalArgumentException("You cannot change the status of your own posts");
        }
    }

    @Transactional
    public PostDto approvePost(Long postId, Long moderatorId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheck(post, moderator);

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
                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return PostService.mapToPostDto(post);
    }

    @Transactional
    public PostDto flagPost(Long postId, Long moderatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for flagging");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));

        moderatorIdCheck(post, moderator);

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
                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        // Notify author
        String message = "Your post '" + post.getTitle() + "' was flagged for review: " + reason;
        notificationService.createNotification(post.getAuthor().getId(), NotificationType.POST_FLAGGED, postId, message);

        return PostService.mapToPostDto(post);
    }

    @Transactional
    public PostDto denyPost(Long postId, Long moderatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for denial");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheck(post, moderator);

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
                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return PostService.mapToPostDto(post);
    }


    public List<CommentDto> getPendingComments() {
        return commentRepository.findByCommentStatus(ContentStatus.PENDING).stream()
                .map(PostService::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto approveComment(Long commentId, Long moderatorId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheck(comment.getPost(), moderator);
        if (comment.getCommentStatus() != ContentStatus.PENDING) {
            throw new IllegalStateException("Only pending comments can be approved");
        }

        ContentStatus previousStatus = comment.getCommentStatus();
        comment.setCommentStatus(ContentStatus.APPROVED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.COMMENT)
                .targetId(commentId)
                .previousStatus(previousStatus)
                .newStatus(ContentStatus.APPROVED)
                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return PostService.mapToCommentDto(comment);
    }

    @Transactional
    public CommentDto flagComment(Long commentId, Long moderatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for flagging");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheck(comment.getPost(), moderator);
        if (comment.getCommentStatus() != ContentStatus.APPROVED && comment.getCommentStatus() != ContentStatus.PENDING) {
            throw new IllegalStateException("Only pending or approved comments can be flagged");
        }

        ContentStatus previousStatus = comment.getCommentStatus();
        comment.setCommentStatus(ContentStatus.FLAGGED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.COMMENT)
                .targetId(commentId)
                .previousStatus(previousStatus)
                .newStatus(ContentStatus.FLAGGED)
                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        // Notify author
        String message = "Your comment was flagged for review: " + reason;
        notificationService.createNotification(comment.getAuthor().getId(), NotificationType.COMMENT_FLAGGED, commentId, message);

        return PostService.mapToCommentDto(comment);
    }

    @Transactional
    public CommentDto denyComment(Long commentId, Long moderatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for denial");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheck(comment.getPost(), moderator);
        if (comment.getCommentStatus() != ContentStatus.PENDING) {
            throw new IllegalStateException("Only pending comments can be denied");
        }

        ContentStatus previousStatus = comment.getCommentStatus();
        comment.setCommentStatus(ContentStatus.DENIED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        ModerationAction action = ModerationAction.builder()
                .type(ModerationType.COMMENT)
                .targetId(commentId)
                .previousStatus(previousStatus)
                .newStatus(ContentStatus.DENIED)
                .reason(reason)
                .moderator(moderator)
                .actionAt(LocalDateTime.now())
                .build();
        moderationActionRepository.save(action);

        return PostService.mapToCommentDto(comment);
    }
}