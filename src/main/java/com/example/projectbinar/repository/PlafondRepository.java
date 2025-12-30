package com.example.projectbinar.repository;

import com.example.projectbinar.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long> {
    List<Plafond> findByIsActiveTrue();
    Optional<Plafond> findByIdAndIsActiveTrue(Long id);
    Optional<Plafond> findByName(String name);
    boolean existsByName(String name);
}
