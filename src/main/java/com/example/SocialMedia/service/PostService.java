package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.entity.Comment;
import com.example.SocialMedia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .postStatus(PostStatus.PENDING)
                .author(author)
                .build();

        post = postRepository.save(post);
        return mapToPostDto(post);
    }

    public List<PostDto> getHomeFeed() {
        return postRepository.findByPostStatus(PostStatus.APPROVED).stream()
                .map(PostService::mapToPostDto)
                .collect(Collectors.toList());
    }

    public PostDetailDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        List<CommentDto> approvedComments = commentRepository.findByPostIdAndCommentStatus(postId, CommentStatus.APPROVED).stream()
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

    public static CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCommentStatus(comment.getCommentStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setAuthor(mapToUserSummaryDto(comment.getAuthor()));
        return dto;
    }

    public static UserSummaryDto mapToUserSummaryDto(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}