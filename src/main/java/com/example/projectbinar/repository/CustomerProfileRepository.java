package com.example.projectbinar.repository;

import com.example.projectbinar.entity.CustomerProfile;
import com.example.projectbinar.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
  Optional<CustomerProfile> findByUser(User user);

  Optional<CustomerProfile> findByUserId(Long userId);

  boolean existsByUser(User user);

  boolean existsByIdentityNumber(String identityNumber);
}
