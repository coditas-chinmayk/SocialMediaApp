package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.Comment;
import com.example.SocialMedia.entity.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
//    List<Comment> findByPostIdAndCommentStatus(Long postId, ContentStatus status);
    List<Comment> findByCommentStatus(ContentStatus status);
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.commentStatus = :status")
    List<Comment> findByPostIdAndCommentStatus(@Param("postId") Long postId, @Param("status") ContentStatus status);

    // New methods for specific status queries
    List<Comment> findByCommentStatusOrderByCreatedAtDesc(ContentStatus commentStatus);
}

