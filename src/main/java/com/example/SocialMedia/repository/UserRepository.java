package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

//import java.lang.ScopedValue;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
   public Optional<User> findById(Long Id);
   public boolean existsByUsername(String username);

   public boolean existsByEmail(String email);

   Optional<User> findByUsername(String username);

   Optional<User> findByEmail(String email);
}
