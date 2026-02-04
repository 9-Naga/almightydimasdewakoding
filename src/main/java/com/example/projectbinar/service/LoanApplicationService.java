package com.example.projectbinar.service;

import com.example.projectbinar.dto.loan.LoanApplicationRequest;
import com.example.projectbinar.dto.loan.LoanApplicationResponse;
import com.example.projectbinar.dto.loan.LoanSimulationRequest;
import com.example.projectbinar.dto.loan.LoanSimulationResponse;
import com.example.projectbinar.entity.CustomerProfile;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.enums.LoanStatus;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.LoanApplicationRepository;
import com.example.projectbinar.service.PlafondService.LoanCalculation;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationService {

  private final LoanApplicationRepository loanApplicationRepository;
  private final CustomerProfileService customerProfileService;
  private final PlafondService plafondService;
  private final NotificationService notificationService;

  public LoanApplicationService(
      LoanApplicationRepository loanApplicationRepository,
      CustomerProfileService customerProfileService,
      PlafondService plafondService,
      NotificationService notificationService) {
    this.loanApplicationRepository = loanApplicationRepository;
    this.customerProfileService = customerProfileService;
    this.plafondService = plafondService;
    this.notificationService = notificationService;
  }

  /**
   * Simulate a loan application without actually creating it. This allows users to see the product,
   * interest rate, and payment details before submitting their application.
   */
  public LoanSimulationResponse simulateLoan(LoanSimulationRequest request) {
    BigDecimal amount = request.getAmount();
    Integer tenorMonth = request.getTenorMonth();

    // Auto-detect plafond based on amount
    Plafond plafond =
        plafondService
            .findPlafondByAmount(amount)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        "No product available for the requested amount of Rp"
                            + formatCurrency(amount)
                            + ". Please check available products."));

    // Validate tenor within plafond limits
    if (tenorMonth > plafond.getTenorMonth()) {
      throw new BadRequestException(
          "Selected tenor ("
              + tenorMonth
              + " months) exceeds maximum allowed ("
              + plafond.getTenorMonth()
              + " months) for product "
              + plafond.getName());
    }

    // Calculate dynamic interest rate
    BigDecimal actualInterestRate =
        plafondService.calculateDynamicInterestRate(
            plafond.getInterestRate(), tenorMonth, plafond.getTenorMonth());

    // Calculate loan details
    LoanCalculation calculation =
        plafondService.calculateLoan(amount, actualInterestRate, tenorMonth);

    return LoanSimulationResponse.builder()
        .plafondId(plafond.getId())
        .plafondName(plafond.getName())
        .amount(amount)
        .tenorMonth(tenorMonth)
        .maxTenorMonth(plafond.getTenorMonth())
        .baseInterestRate(plafond.getInterestRate())
        .actualInterestRate(actualInterestRate)
        .totalInterest(calculation.getTotalInterest())
        .totalPayment(calculation.getTotalPayment())
        .monthlyInstallment(calculation.getMonthlyInstallment())
        .message(
            "Simulation successful! You qualify for product "
                + plafond.getName()
                + " with interest rate "
                + actualInterestRate
                + "% per annum.")
        .build();
  }

  @Transactional
  public LoanApplicationResponse createLoanApplication(
      Long userId, LoanApplicationRequest request) {
    // Check if customer has complete profile
    if (!customerProfileService.hasCompleteProfile(userId)) {
      throw new BadRequestException(
          "Please complete your profile including KTP upload before applying for a loan");
    }

    BigDecimal amount = request.getAmount();
    Integer tenorMonth = request.getTenorMonth();

    // Auto-detect plafond based on amount
    Plafond plafond =
        plafondService
            .findPlafondByAmount(amount)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        "No product available for the requested amount of Rp"
                            + formatCurrency(amount)
                            + ". Please check available products."));

    // Validate tenor within plafond limits
    if (tenorMonth > plafond.getTenorMonth()) {
      throw new BadRequestException(
          "Selected tenor ("
              + tenorMonth
              + " months) exceeds maximum allowed ("
              + plafond.getTenorMonth()
              + " months) for product "
              + plafond.getName());
    }

    if (tenorMonth < 1) {
      throw new BadRequestException("Tenor must be at least 1 month");
    }

    CustomerProfile customer = customerProfileService.getProfileEntityByUserId(userId);

    // Calculate dynamic interest rate
    BigDecimal actualInterestRate =
        plafondService.calculateDynamicInterestRate(
            plafond.getInterestRate(), tenorMonth, plafond.getTenorMonth());

    // Calculate loan details
    LoanCalculation calculation =
        plafondService.calculateLoan(amount, actualInterestRate, tenorMonth);

    LoanApplication loanApplication =
        LoanApplication.builder()
            .customer(customer)
            .plafond(plafond)
            .amount(amount)
            .tenorMonth(tenorMonth)
            .interestRate(actualInterestRate)
            .totalInterest(calculation.getTotalInterest())
            .totalPayment(calculation.getTotalPayment())
            .monthlyInstallment(calculation.getMonthlyInstallment())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .status(LoanStatus.SUBMITTED)
            .build();

    LoanApplication savedApplication = loanApplicationRepository.save(loanApplication);

    // Send notification
    notificationService.sendLoanSubmittedNotification(savedApplication);

    return mapToResponse(savedApplication);
  }

  public List<LoanApplicationResponse> getLoansByCustomerId(Long customerId) {
    return loanApplicationRepository.findByCustomerId(customerId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<LoanApplicationResponse> getLoansByUserId(Long userId) {
    CustomerProfile customer = customerProfileService.getProfileEntityByUserId(userId);
    return loanApplicationRepository.findByCustomer(customer).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public LoanApplicationResponse getLoanById(Long id) {
    LoanApplication loan =
        loanApplicationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Loan application", "id", id));
    return mapToResponse(loan);
  }

  public LoanApplication getLoanEntityById(Long id) {
    return loanApplicationRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Loan application", "id", id));
  }

  public List<LoanApplicationResponse> getLoansByStatus(LoanStatus status) {
    return loanApplicationRepository.findByStatus(status).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<LoanApplicationResponse> getPendingReviewLoans() {
    return loanApplicationRepository.findByStatus(LoanStatus.SUBMITTED).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<LoanApplicationResponse> getReviewedLoans() {
    return loanApplicationRepository.findByStatus(LoanStatus.IN_REVIEW).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<LoanApplicationResponse> getApprovedLoans() {
    return loanApplicationRepository.findByStatus(LoanStatus.APPROVED).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void updateLoanStatus(Long loanId, LoanStatus status) {
    LoanApplication loan = getLoanEntityById(loanId);
    loan.setStatus(status);
    loanApplicationRepository.save(loan);
  }

  private LoanApplicationResponse mapToResponse(LoanApplication loan) {
    return LoanApplicationResponse.builder()
        .id(loan.getId())
        .userId(loan.getCustomer().getUser().getId())
        .customerId(loan.getCustomer().getId())
        .customerName(loan.getCustomer().getFullName())
        .plafondId(loan.getPlafond().getId())
        .plafondName(loan.getPlafond().getName())
        .amount(loan.getAmount())
        .tenorMonth(loan.getTenorMonth())
        .maxTenorMonth(loan.getPlafond().getTenorMonth())
        .baseInterestRate(loan.getPlafond().getInterestRate())
        .actualInterestRate(loan.getInterestRate())
        .totalInterest(loan.getTotalInterest())
        .totalPayment(loan.getTotalPayment())
        .monthlyInstallment(loan.getMonthlyInstallment())
        .status(loan.getStatus())
        .createdAt(loan.getCreatedAt())
        .updatedAt(loan.getUpdatedAt())
        .latitude(loan.getLatitude())
        .longitude(loan.getLongitude())
        .build();
  }

  private String formatCurrency(BigDecimal amount) {
    return String.format("%,.0f", amount);
  }
}
