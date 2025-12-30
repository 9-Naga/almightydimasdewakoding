package com.example.projectbinar.service;

import com.example.projectbinar.dto.loan.LoanApprovalRequest;
import com.example.projectbinar.dto.loan.LoanApprovalResponse;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.LoanApproval;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.ApprovalStatus;
import com.example.projectbinar.enums.LoanStatus;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.LoanApprovalRepository;
import com.example.projectbinar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanApprovalService {

    private final LoanApprovalRepository loanApprovalRepository;
    private final LoanApplicationService loanApplicationService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public LoanApprovalService(LoanApprovalRepository loanApprovalRepository,
                               LoanApplicationService loanApplicationService,
                               NotificationService notificationService,
                               UserRepository userRepository) {
        this.loanApprovalRepository = loanApprovalRepository;
        this.loanApplicationService = loanApplicationService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoanApprovalResponse submitApproval(Long loanId, Long approverId, LoanApprovalRequest request) {
        LoanApplication loan = loanApplicationService.getLoanEntityById(loanId);

        // Validate loan status - must be IN_REVIEW (reviewed by marketing)
        if (loan.getStatus() != LoanStatus.IN_REVIEW) {
            throw new BadRequestException("Loan application must be reviewed by marketing first (status: IN_REVIEW)");
        }

        // Check if already approved
        if (loanApprovalRepository.existsByLoanApplication(loan)) {
            throw new BadRequestException("This loan application has already been processed");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));

        LoanApproval approval = LoanApproval.builder()
                .loanApplication(loan)
                .approver(approver)
                .approvalStatus(request.getApprovalStatus())
                .approvalNote(request.getApprovalNote())
                .build();

        LoanApproval savedApproval = loanApprovalRepository.save(approval);

        // Update loan status based on approval
        if (request.getApprovalStatus() == ApprovalStatus.APPROVED) {
            loanApplicationService.updateLoanStatus(loanId, LoanStatus.APPROVED);
            notificationService.sendLoanApprovedNotification(loan);
        } else if (request.getApprovalStatus() == ApprovalStatus.REJECTED) {
            loanApplicationService.updateLoanStatus(loanId, LoanStatus.REJECT);
            notificationService.sendLoanRejectedNotification(loan, request.getApprovalNote());
        }

        return mapToResponse(savedApproval);
    }

    public List<LoanApprovalResponse> getApprovalsByApprover(Long approverId) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));
        return loanApprovalRepository.findByApprover(approver).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LoanApprovalResponse getApprovalByLoanId(Long loanId) {
        LoanApproval approval = loanApprovalRepository.findByLoanApplicationId(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found for loan: " + loanId));
        return mapToResponse(approval);
    }

    private LoanApprovalResponse mapToResponse(LoanApproval approval) {
        return LoanApprovalResponse.builder()
                .id(approval.getId())
                .loanApplicationId(approval.getLoanApplication().getId())
                .approverId(approval.getApprover().getId())
                .approverName(approval.getApprover().getFullname())
                .approvalStatus(approval.getApprovalStatus())
                .approvalNote(approval.getApprovalNote())
                .approvalDate(approval.getApprovalDate())
                .build();
    }
}
