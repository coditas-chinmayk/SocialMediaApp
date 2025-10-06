package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.Comment;
import com.example.SocialMedia.entity.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndCommentStatus(Long postId, ContentStatus status);
    List<Comment> findByCommentStatus(ContentStatus status);
}