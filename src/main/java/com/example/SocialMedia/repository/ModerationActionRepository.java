package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, Long> {
}