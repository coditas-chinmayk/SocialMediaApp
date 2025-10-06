package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.CommentStatus;
import com.example.SocialMedia.entity.Post;
import com.example.SocialMedia.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPostStatus(PostStatus status);
    List<Post> findByAuthorIdAndPostStatus(Long authorId, PostStatus status);
    List<Post> findByPostIdAndCommentStatus(Long postId, CommentStatus status);

}