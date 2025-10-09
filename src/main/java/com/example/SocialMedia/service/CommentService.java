package com.example.SocialMedia.service;

import com.example.SocialMedia.Constants.ModerationType;
import com.example.SocialMedia.Constants.NotificationType;
import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.CreateCommentRequest;
import com.example.SocialMedia.entity.Comment;
import com.example.SocialMedia.Constants.ContentStatus;
import com.example.SocialMedia.entity.ModerationAction;
import com.example.SocialMedia.entity.Post;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.repository.CommentRepository;
import com.example.SocialMedia.repository.ModerationActionRepository;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private UserRepository userRepository;
    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private ModerationActionRepository moderationActionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    public CommentService(UserRepository userRepository, CommentRepository commentRepository, PostRepository postRepository, ModerationActionRepository moderationActionRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.moderationActionRepository = moderationActionRepository;
    }

    @Transactional
    public CommentDto createComment(Long userId, Long postId, CreateCommentRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .commentStatus(ContentStatus.PENDING)
                .author(author)
                .post(post)
                .build();

        comment = commentRepository.save(comment);
        return PostService.mapToCommentDto(comment);
    }

    @Transactional
    public CommentDto editFlaggedComment(Long commentId, Long userId, CreateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the comment author can edit this comment");
        }

        if (comment.getCommentStatus() != ContentStatus.FLAGGED) {
            throw new IllegalStateException("Only flagged comments can be edited");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        comment.setContent(request.getContent());
        comment.setCommentStatus(ContentStatus.PENDING);
        comment.setUpdatedAt(LocalDateTime.now());

        comment = commentRepository.save(comment);
        return PostService.mapToCommentDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        // Only the comment author can delete the comment
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException(" Only the comment author can delete this comment");
        }

        commentRepository.delete(comment);
    }

    public List<CommentDto> getCommentsByStatus(ContentStatus status) {
        return commentRepository.findByCommentStatusOrderByCreatedAtDesc(status).stream()
                .map(PostService::mapToCommentDto)
                .collect(Collectors.toList());
    }


    public void moderatorIdCheckForComment(Comment comment, User moderator) throws IllegalArgumentException{
        if (comment.getAuthor().getId().equals(moderator.getId())) {
            throw new IllegalArgumentException("You cannot change the status of your own comments");
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public CommentDto approveComment(Long commentId, Long moderatorId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheckForComment(comment, moderator);
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

    @org.springframework.transaction.annotation.Transactional
    public CommentDto flagComment(Long commentId, Long moderatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for flagging");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheckForComment(comment, moderator);
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

    @org.springframework.transaction.annotation.Transactional
    public CommentDto denyComment(Long commentId, Long moderatorId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required for denial");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NoSuchElementException("Moderator not found"));
        moderatorIdCheckForComment(comment, moderator);
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


    public List<CommentDto> getApprovedComments() {
        return getCommentsByStatus(ContentStatus.APPROVED);
    }

    public List<CommentDto> getPendingComments() {
        return getCommentsByStatus(ContentStatus.PENDING);
    }

    public List<CommentDto> getFlaggedComments() {
        return getCommentsByStatus(ContentStatus.FLAGGED);
    }

    public List<CommentDto> getDeniedComments() {
        return getCommentsByStatus(ContentStatus.DENIED);
    }
}