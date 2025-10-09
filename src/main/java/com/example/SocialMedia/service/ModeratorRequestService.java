package com.example.SocialMedia.service;

import com.example.SocialMedia.Constants.RequestStatus;
import com.example.SocialMedia.entity.*;
import com.example.SocialMedia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModeratorRequestService {

    @Autowired
    private ModeratorRequestRepository moderatorRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ModeratorRequest createModeratorRequest(Long userId) throws Throwable {
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("No user found by this id "));

        if (moderatorRequestRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Moderator request already exists for this user");
        }

        ModeratorRequest request = ModeratorRequest.builder()
                .requestStatus(RequestStatus.PENDING)
                .user(user)
                .build();

        return moderatorRequestRepository.save(request);
    }
}