package com.example.projectbinar.repository;

import com.example.projectbinar.entity.Plafond;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long> {
  List<Plafond> findByIsActiveTrue();

  Optional<Plafond> findByIdAndIsActiveTrue(Long id);

  Optional<Plafond> findByName(String name);

  boolean existsByName(String name);

  /**
   * Find active plafond by amount (auto-detect product) Returns plafond where minAmount <= amount
   * <= maxAmount
   */
  @Query(
      "SELECT p FROM Plafond p WHERE p.isActive = true AND p.minAmount <= :amount AND p.maxAmount"
          + " >= :amount")
  Optional<Plafond> findActiveByAmount(@Param("amount") BigDecimal amount);

  /**
   * Check for overlapping ranges with other active plafonds Used when creating or updating a
   * plafond to prevent range conflicts
   */
  @Query(
      "SELECT p FROM Plafond p WHERE p.isActive = true AND p.id != :excludeId "
          + "AND ((p.minAmount <= :maxAmount AND p.maxAmount >= :minAmount))")
  List<Plafond> findOverlappingPlafonds(
      @Param("excludeId") Long excludeId,
      @Param("minAmount") BigDecimal minAmount,
      @Param("maxAmount") BigDecimal maxAmount);

  /** Check for overlapping ranges (for new plafond creation) */
  @Query(
      "SELECT p FROM Plafond p WHERE p.isActive = true "
          + "AND ((p.minAmount <= :maxAmount AND p.maxAmount >= :minAmount))")
  List<Plafond> findOverlappingPlafondsForNew(
      @Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);
}
