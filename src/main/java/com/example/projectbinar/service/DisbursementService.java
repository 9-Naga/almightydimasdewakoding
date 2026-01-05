package com.example.projectbinar.service;

import com.example.projectbinar.dto.loan.DisbursementResponse;
import com.example.projectbinar.entity.Disbursement;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.DisbursementStatus;
import com.example.projectbinar.enums.LoanStatus;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.DisbursementRepository;
import com.example.projectbinar.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisbursementService {

  private final DisbursementRepository disbursementRepository;
  private final LoanApplicationService loanApplicationService;
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  public DisbursementService(
      DisbursementRepository disbursementRepository,
      LoanApplicationService loanApplicationService,
      NotificationService notificationService,
      UserRepository userRepository) {
    this.disbursementRepository = disbursementRepository;
    this.loanApplicationService = loanApplicationService;
    this.notificationService = notificationService;
    this.userRepository = userRepository;
  }

  @Transactional
  public DisbursementResponse processDisbursement(Long loanId, Long processedById) {
    LoanApplication loan = loanApplicationService.getLoanEntityById(loanId);

    // Validate loan status - must be APPROVED
    if (loan.getStatus() != LoanStatus.APPROVED) {
      throw new BadRequestException(
          "Loan application must be approved before disbursement (status: APPROVED)");
    }

    // Check if already disbursed
    if (disbursementRepository.existsByLoanApplication(loan)) {
      throw new BadRequestException("This loan application has already been disbursed");
    }

    User processedBy =
        userRepository
            .findById(processedById)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", processedById));

    Disbursement disbursement =
        Disbursement.builder()
            .loanApplication(loan)
            .disbursementAmount(loan.getAmount())
            .status(DisbursementStatus.COMPLETED)
            .processedBy(processedBy)
            .build();

    Disbursement savedDisbursement = disbursementRepository.save(disbursement);

    // Update loan status
    loanApplicationService.updateLoanStatus(loanId, LoanStatus.DISBURSE);

    // Send notification
    notificationService.sendLoanDisbursedNotification(loan);

    return mapToResponse(savedDisbursement);
  }

  public List<DisbursementResponse> getAllDisbursements() {
    return disbursementRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<DisbursementResponse> getDisbursementsByStatus(DisbursementStatus status) {
    return disbursementRepository.findByStatus(status).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public DisbursementResponse getDisbursementByLoanId(Long loanId) {
    Disbursement disbursement =
        disbursementRepository
            .findByLoanApplicationId(loanId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Disbursement not found for loan: " + loanId));
    return mapToResponse(disbursement);
  }

  private DisbursementResponse mapToResponse(Disbursement disbursement) {
    return DisbursementResponse.builder()
        .id(disbursement.getId())
        .loanApplicationId(disbursement.getLoanApplication().getId())
        .disbursementAmount(disbursement.getDisbursementAmount())
        .disbursementDate(disbursement.getDisbursementDate())
        .status(disbursement.getStatus())
        .processedById(
            disbursement.getProcessedBy() != null ? disbursement.getProcessedBy().getId() : null)
        .processedByName(
            disbursement.getProcessedBy() != null
                ? disbursement.getProcessedBy().getFullname()
                : null)
        .build();
  }
}
