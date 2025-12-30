package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.loan.LoanApplicationResponse;
import com.example.projectbinar.dto.loan.LoanApprovalRequest;
import com.example.projectbinar.dto.loan.LoanApprovalResponse;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.service.AuthService;
import com.example.projectbinar.service.LoanApplicationService;
import com.example.projectbinar.service.LoanApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@Tag(name = "Loan Approval", description = "Loan approval operations for BRANCH_MANAGER role")
@SecurityRequirement(name = "Bearer Authentication")
public class ApprovalController {

    private final LoanApprovalService loanApprovalService;
    private final LoanApplicationService loanApplicationService;
    private final AuthService authService;

    public ApprovalController(LoanApprovalService loanApprovalService, LoanApplicationService loanApplicationService, AuthService authService) {
        this.loanApprovalService = loanApprovalService;
        this.loanApplicationService = loanApplicationService;
        this.authService = authService;
    }

    @GetMapping("/pending")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, 'LOAN_APPROVE') or hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Get pending approvals", description = "BRANCH_MANAGER - Get reviewed loans pending approval")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getPendingApprovals() {
        List<LoanApplicationResponse> loans = loanApplicationService.getReviewedLoans();
        
        ApiResponse<List<LoanApplicationResponse>> response = ApiResponse.<List<LoanApplicationResponse>>builder()
                .success(true)
                .message("Pending approvals retrieved successfully")
                .data(loans)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{loanId}")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, 'LOAN_APPROVE') or hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Submit approval", description = "BRANCH_MANAGER - Approve or reject loan application")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> submitApproval(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanApprovalRequest request) {
        CustomUserDetails currentUser = authService.getCurrentUser();
        LoanApprovalResponse approval = loanApprovalService.submitApproval(loanId, currentUser.getId(), request);
        
        ApiResponse<LoanApprovalResponse> response = ApiResponse.<LoanApprovalResponse>builder()
                .success(true)
                .message("Approval submitted successfully")
                .data(approval)
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-approvals")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, 'LOAN_APPROVE') or hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Get my approvals", description = "BRANCH_MANAGER - Get approvals by current user")
    public ResponseEntity<ApiResponse<List<LoanApprovalResponse>>> getMyApprovals() {
        CustomUserDetails currentUser = authService.getCurrentUser();
        List<LoanApprovalResponse> approvals = loanApprovalService.getApprovalsByApprover(currentUser.getId());
        
        ApiResponse<List<LoanApprovalResponse>> response = ApiResponse.<List<LoanApprovalResponse>>builder()
                .success(true)
                .message("Approvals retrieved successfully")
                .data(approvals)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan/{loanId}")
    @PreAuthorize("hasAnyRole('MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE', 'SUPER_ADMIN')")
    @Operation(summary = "Get approval by loan ID", description = "Get approval details for a specific loan")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> getApprovalByLoanId(@PathVariable Long loanId) {
        LoanApprovalResponse approval = loanApprovalService.getApprovalByLoanId(loanId);
        
        ApiResponse<LoanApprovalResponse> response = ApiResponse.<LoanApprovalResponse>builder()
                .success(true)
                .message("Approval retrieved successfully")
                .data(approval)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
