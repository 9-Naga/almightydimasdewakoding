package com.example.projectbinar.repository;

import com.example.projectbinar.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  List<User> findByIsActiveTrue();

  Optional<User> findByIdAndIsActiveTrue(Long id);

  Optional<User> findByEmail(String email);
}
