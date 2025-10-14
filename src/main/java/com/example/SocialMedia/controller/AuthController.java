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

import java.util.Map;
import java.util.Set;
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

        // Basic validation (add more in production, e.g., check null/empty via @Valid or manually)
        if (username == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "Username and password are required", null));
        }

        String token = userService.login(username, password);
        User user = userService.getUserFromUsername(username);

        // Assuming UserResponseDTO has a setRole(String role) setter; adjust if needed
        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(user.getId());
        userResponse.setToken(token);
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());

        // Compute highest role (null-safe)
        Set<Long> roleIds = (user.getRoles() != null)
                ? user.getRoles().stream().map(Role::getId).collect(Collectors.toSet())
                : Set.of();
        String finalRole = getHighestRole(roleIds);
        userResponse.setRole(finalRole);  // Changed from setRoles(Set<String>) to setRole(String)

        return ResponseEntity.ok(new ApiResponseDto<>(true, "login successful", userResponse));
    }

    // Extracted private method for highest role logic (cleaner and reusable)
    private String getHighestRole(Set<Long> roleIds) {
        if (roleIds.contains(4L)) return "SUPER_ADMIN";
        if (roleIds.contains(3L)) return "ADMIN";
        if (roleIds.contains(2L)) return "MODERATOR";
        return "AUTHOR";
    }
}