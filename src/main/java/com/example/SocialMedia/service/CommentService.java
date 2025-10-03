package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.CommentDto;
import com.example.SocialMedia.dto.CreateCommentRequest;
import com.example.SocialMedia.dto.UserSummaryDto;
import com.example.SocialMedia.entity.Comment;
import com.example.SocialMedia.entity.CommentStatus;
import com.example.SocialMedia.entity.Post;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.repository.CommentRepository;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CommentService {

    private UserRepository userRepository;
    private CommentRepository commentRepository;
    private PostRepository postRepository;

    @Autowired
    public CommentService(UserRepository userRepository, CommentRepository commentRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
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
                .commentStatus(CommentStatus.PENDING)
                .author(author)
                .post(post)
                .build();

        comment = commentRepository.save(comment);
        return PostService.mapToCommentDto(comment);
    }
}
