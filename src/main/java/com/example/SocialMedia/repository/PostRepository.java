package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.ContentStatus;
import com.example.SocialMedia.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPostStatus(ContentStatus postStatus);
    List<Post> findByAuthorId(Long authorId);

    // New methods for specific status queries
    List<Post> findByPostStatusOrderByCreatedAtDesc(ContentStatus postStatus);
}