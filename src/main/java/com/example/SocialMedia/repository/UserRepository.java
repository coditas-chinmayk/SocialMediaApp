package com.example.SocialMedia.repository;

import com.example.SocialMedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByUsername(String username);
   Optional<User> findByEmail(String email);
   boolean existsByUsername(String username);
   boolean existsByEmail(String email);


   @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'MODERATOR'")
   List<User> findAllModerators();

   @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN' OR r.name = 'SUPER_ADMIN'")
   List<User> findAllAdmins();

   @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
   List<User> findByRoleName(@Param("roleName") String roleName);
}