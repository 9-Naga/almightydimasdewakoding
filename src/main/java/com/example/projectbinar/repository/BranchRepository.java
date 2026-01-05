package com.example.projectbinar.repository;

import com.example.projectbinar.entity.Branch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
  Optional<Branch> findByName(String name);
}
