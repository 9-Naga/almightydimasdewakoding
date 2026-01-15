package com.example.projectbinar.service;

import com.example.projectbinar.dto.plafond.PlafondDetectionResponse;
import com.example.projectbinar.dto.plafond.PlafondRequest;
import com.example.projectbinar.dto.plafond.PlafondResponse;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.PlafondRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlafondService {

  private final PlafondRepository plafondRepository;

  public PlafondService(PlafondRepository plafondRepository) {
    this.plafondRepository = plafondRepository;
  }

  public List<PlafondResponse> getAllActivePlafonds() {
    return plafondRepository.findByIsActiveTrue().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<PlafondResponse> getAllPlafonds() {
    return plafondRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public PlafondResponse getPlafondById(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));
    return mapToResponse(plafond);
  }

  public Plafond getPlafondEntityById(Long id) {
    return plafondRepository
        .findByIdAndIsActiveTrue(id)
        .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));
  }

  /**
   * Auto-detect plafond based on loan amount. Returns the active plafond where minAmount <= amount
   * <= maxAmount.
   */
  public Optional<Plafond> findPlafondByAmount(BigDecimal amount) {
    return plafondRepository.findActiveByAmount(amount);
  }

  /**
   * Get plafond detection response for a given amount. This is used by the frontend to show the
   * user which product they qualify for.
   */
  public PlafondDetectionResponse detectPlafondByAmount(BigDecimal amount) {
    Optional<Plafond> plafondOpt = findPlafondByAmount(amount);

    if (plafondOpt.isEmpty()) {
      return PlafondDetectionResponse.builder()
          .found(false)
          .message(
              "No product available for the requested amount of Rp"
                  + formatCurrency(amount)
                  + ". Please check available products.")
          .build();
    }

    Plafond plafond = plafondOpt.get();
    List<Integer> availableTenors =
        IntStream.rangeClosed(1, plafond.getTenorMonth()).boxed().collect(Collectors.toList());

    return PlafondDetectionResponse.builder()
        .found(true)
        .plafondId(plafond.getId())
        .plafondName(plafond.getName())
        .minAmount(plafond.getMinAmount())
        .maxAmount(plafond.getMaxAmount())
        .baseInterestRate(plafond.getInterestRate())
        .maxTenorMonth(plafond.getTenorMonth())
        .availableTenors(availableTenors)
        .message(
            "Product "
                + plafond.getName()
                + " is available for your loan amount. Maximum tenor: "
                + plafond.getTenorMonth()
                + " months.")
        .build();
  }

  /**
   * Calculate dynamic interest rate based on tenor.
   *
   * <p>Formula: actualRate = baseRate * (selectedTenor / maxTenor)
   *
   * <p>This makes shorter tenors have lower interest rates. Example: If baseRate = 10%, maxTenor =
   * 12, selectedTenor = 6 actualRate = 10% * (6/12) = 5%
   *
   * <p>Minimum rate is set to 50% of base rate to ensure profitability.
   */
  public BigDecimal calculateDynamicInterestRate(
      BigDecimal baseInterestRate, Integer selectedTenor, Integer maxTenor) {
    if (selectedTenor == null || maxTenor == null || maxTenor == 0) {
      return baseInterestRate;
    }

    // Calculate ratio: selectedTenor / maxTenor
    BigDecimal tenorRatio =
        new BigDecimal(selectedTenor).divide(new BigDecimal(maxTenor), 4, RoundingMode.HALF_UP);

    // Apply minimum floor of 0.5 (50% of base rate)
    BigDecimal minRatio = new BigDecimal("0.5");
    if (tenorRatio.compareTo(minRatio) < 0) {
      tenorRatio = minRatio;
    }

    // Calculate actual rate
    BigDecimal actualRate = baseInterestRate.multiply(tenorRatio).setScale(2, RoundingMode.HALF_UP);

    return actualRate;
  }

  /**
   * Calculate loan details using flat interest method.
   *
   * <p>Formulas: - totalInterest = principal × (interestRate / 100) × (tenor / 12) - totalPayment =
   * principal + totalInterest - monthlyInstallment = totalPayment / tenor
   */
  public LoanCalculation calculateLoan(
      BigDecimal principal, BigDecimal interestRate, Integer tenorMonth) {
    // Calculate total interest using flat rate formula
    BigDecimal totalInterest =
        principal
            .multiply(interestRate)
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(tenorMonth))
            .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

    // Calculate total payment
    BigDecimal totalPayment = principal.add(totalInterest);

    // Calculate monthly installment
    BigDecimal monthlyInstallment =
        totalPayment.divide(new BigDecimal(tenorMonth), 2, RoundingMode.HALF_UP);

    return new LoanCalculation(totalInterest, totalPayment, monthlyInstallment);
  }

  @Transactional
  public PlafondResponse createPlafond(PlafondRequest request) {
    if (plafondRepository.existsByName(request.getName())) {
      throw new BadRequestException("Plafond with name '" + request.getName() + "' already exists");
    }

    if (request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
      throw new BadRequestException("Minimum amount cannot be greater than maximum amount");
    }

    // Check for overlapping ranges with active plafonds
    validateNoOverlap(null, request.getMinAmount(), request.getMaxAmount());

    Plafond plafond =
        Plafond.builder()
            .name(request.getName())
            .minAmount(request.getMinAmount())
            .maxAmount(request.getMaxAmount())
            .interestRate(request.getInterestRate())
            .tenorMonth(request.getTenorMonth())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();

    Plafond savedPlafond = plafondRepository.save(plafond);
    return mapToResponse(savedPlafond);
  }

  @Transactional
  public PlafondResponse updatePlafond(Long id, PlafondRequest request) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));

    // Check if new name conflicts with existing
    if (!plafond.getName().equals(request.getName())
        && plafondRepository.existsByName(request.getName())) {
      throw new BadRequestException("Plafond with name '" + request.getName() + "' already exists");
    }

    if (request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
      throw new BadRequestException("Minimum amount cannot be greater than maximum amount");
    }

    // Check for overlapping ranges (if this plafond will be active)
    Boolean willBeActive =
        request.getIsActive() != null ? request.getIsActive() : plafond.getIsActive();
    if (willBeActive) {
      validateNoOverlap(id, request.getMinAmount(), request.getMaxAmount());
    }

    plafond.setName(request.getName());
    plafond.setMinAmount(request.getMinAmount());
    plafond.setMaxAmount(request.getMaxAmount());
    plafond.setInterestRate(request.getInterestRate());
    plafond.setTenorMonth(request.getTenorMonth());
    if (request.getIsActive() != null) {
      plafond.setIsActive(request.getIsActive());
    }

    Plafond savedPlafond = plafondRepository.save(plafond);
    return mapToResponse(savedPlafond);
  }

  @Transactional
  public void deactivatePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));
    plafond.setIsActive(false);
    plafondRepository.save(plafond);
  }

  @Transactional
  public PlafondResponse activatePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plafond", "id", id));

    // Check for overlapping ranges before activating
    validateNoOverlap(id, plafond.getMinAmount(), plafond.getMaxAmount());

    plafond.setIsActive(true);
    Plafond savedPlafond = plafondRepository.save(plafond);
    return mapToResponse(savedPlafond);
  }

  /** Validate that no active plafond has overlapping amount ranges. */
  private void validateNoOverlap(Long excludeId, BigDecimal minAmount, BigDecimal maxAmount) {
    List<Plafond> overlapping;
    if (excludeId != null) {
      overlapping = plafondRepository.findOverlappingPlafonds(excludeId, minAmount, maxAmount);
    } else {
      overlapping = plafondRepository.findOverlappingPlafondsForNew(minAmount, maxAmount);
    }

    if (!overlapping.isEmpty()) {
      String conflictingNames =
          overlapping.stream().map(Plafond::getName).collect(Collectors.joining(", "));
      throw new BadRequestException(
          "Amount range overlaps with existing active product(s): "
              + conflictingNames
              + ". Please adjust the min/max amount to avoid conflicts.");
    }
  }

  private PlafondResponse mapToResponse(Plafond plafond) {
    return PlafondResponse.builder()
        .id(plafond.getId())
        .name(plafond.getName())
        .minAmount(plafond.getMinAmount())
        .maxAmount(plafond.getMaxAmount())
        .interestRate(plafond.getInterestRate())
        .tenorMonth(plafond.getTenorMonth())
        .isActive(plafond.getIsActive())
        .build();
  }

  private String formatCurrency(BigDecimal amount) {
    return String.format("%,.0f", amount);
  }

  /** Inner class to hold loan calculation results. */
  public static class LoanCalculation {
    private final BigDecimal totalInterest;
    private final BigDecimal totalPayment;
    private final BigDecimal monthlyInstallment;

    public LoanCalculation(
        BigDecimal totalInterest, BigDecimal totalPayment, BigDecimal monthlyInstallment) {
      this.totalInterest = totalInterest;
      this.totalPayment = totalPayment;
      this.monthlyInstallment = monthlyInstallment;
    }

    public BigDecimal getTotalInterest() {
      return totalInterest;
    }

    public BigDecimal getTotalPayment() {
      return totalPayment;
    }

    public BigDecimal getMonthlyInstallment() {
      return monthlyInstallment;
    }
  }
}
