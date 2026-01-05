package com.example.projectbinar.service;

import com.example.projectbinar.dto.loan.LoanApplicationRequest;
import com.example.projectbinar.dto.loan.LoanApplicationResponse;
import com.example.projectbinar.entity.CustomerProfile;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.enums.LoanStatus;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.LoanApplicationRepository;
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

  @Transactional
  public LoanApplicationResponse createLoanApplication(
      Long userId, LoanApplicationRequest request) {
    // Check if customer has complete profile
    if (!customerProfileService.hasCompleteProfile(userId)) {
      throw new BadRequestException(
          "Please complete your profile including KTP upload before applying for a loan");
    }

    CustomerProfile customer = customerProfileService.getProfileEntityByUserId(userId);
    Plafond plafond = plafondService.getPlafondEntityById(request.getPlafondId());

    // Validate amount within plafond limits
    if (request.getAmount().compareTo(plafond.getMinAmount()) < 0) {
      throw new BadRequestException("Amount must be at least " + plafond.getMinAmount());
    }
    if (request.getAmount().compareTo(plafond.getMaxAmount()) > 0) {
      throw new BadRequestException("Amount cannot exceed " + plafond.getMaxAmount());
    }

    LoanApplication loanApplication =
        LoanApplication.builder()
            .customer(customer)
            .plafond(plafond)
            .amount(request.getAmount())
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
        .customerId(loan.getCustomer().getId())
        .customerName(loan.getCustomer().getFullName())
        .plafondId(loan.getPlafond().getId())
        .plafondName(loan.getPlafond().getName())
        .amount(loan.getAmount())
        .interestRate(loan.getPlafond().getInterestRate())
        .tenorMonth(loan.getPlafond().getTenorMonth())
        .status(loan.getStatus())
        .createdAt(loan.getCreatedAt())
        .updatedAt(loan.getUpdatedAt())
        .build();
  }
}
