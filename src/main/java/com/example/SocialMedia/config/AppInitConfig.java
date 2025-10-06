package com.example.SocialMedia.config;

import com.example.SocialMedia.entity.Role;
import com.example.SocialMedia.entity.User;
import com.example.SocialMedia.entity.UserStatus;
import com.example.SocialMedia.repository.RoleRepository;
import com.example.SocialMedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class AppInitConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initSuperAdmin() {
        return args -> {
            // Seed roles if they don't exist
            if (roleRepository.findByName("AUTHOR").isEmpty()) {
                roleRepository.save(Role.builder().name("AUTHOR").build());
            }
            if (roleRepository.findByName("MODERATOR").isEmpty()) {
                roleRepository.save(Role.builder().name("MODERATOR").build());
            }
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                roleRepository.save(Role.builder().name("ADMIN").build());
            }
            if (roleRepository.findByName("SUPER_ADMIN").isEmpty()) {
                roleRepository.save(Role.builder().name("SUPER_ADMIN").build());
            }

            // Seed Super Admin user
            if (userRepository.findByUsername("superadmin").isEmpty()) {
                Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                        .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                Role moderatorRole = roleRepository.findByName("MODERATOR")
                        .orElseThrow(() -> new RuntimeException("MODERATOR role not found"));
                Role authorRole = roleRepository.findByName("AUTHOR")
                        .orElseThrow(() -> new RuntimeException("AUTHOR role not found"));

                Set<Role> roles = new HashSet<>();
                roles.add(superAdminRole);
                roles.add(adminRole);
                roles.add(moderatorRole);
                roles.add(authorRole);

                User superAdmin = User.builder()
                        .username("superadmin")
                        .email("superadmin@example.com")
                        .password(passwordEncoder.encode("super@123"))
                        .status(UserStatus.ACTIVE)
                        .roles(roles)
                        .build();

                userRepository.save(superAdmin);
            }
        };
    }
}