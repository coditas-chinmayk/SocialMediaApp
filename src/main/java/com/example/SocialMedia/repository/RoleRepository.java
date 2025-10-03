package com.example.SocialMedia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SocialMedia.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByName(String role);
}
