package com.example.SocialMedia.repository;

import com.example.SocialMedia.Constants.RequestStatus;
import com.example.SocialMedia.entity.ModeratorRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModeratorRequestRepository extends JpaRepository<ModeratorRequest, Long> {
    Optional<ModeratorRequest> findByUserId(Long userId);
    boolean existsByUserIdAndRequestStatus(Long user_id, RequestStatus requestStatus);
}