package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ApiResponseDto;
import com.example.SocialMedia.dto.UserResponseDTO;
import com.example.SocialMedia.entity.Role;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<UserResponseDTO>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        String token = userService.login(username, password);
        User user = userService.getUserFromUsername(username);

        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(user.getId());
        userResponse.setToken(token);
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        return ResponseEntity.ok(new ApiResponseDto<>(true, "login successful", userResponse));
    }
}