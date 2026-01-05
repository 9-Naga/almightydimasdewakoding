package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.loan.DisbursementResponse;
import com.example.projectbinar.dto.loan.LoanApplicationResponse;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.service.AuthService;
import com.example.projectbinar.service.DisbursementService;
import com.example.projectbinar.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disbursements")
@Tag(name = "Disbursement", description = "Loan disbursement operations for BACK_OFFICE role")
@SecurityRequirement(name = "Bearer Authentication")
public class DisbursementController {

  private final DisbursementService disbursementService;
  private final LoanApplicationService loanApplicationService;
  private final AuthService authService;

  public DisbursementController(
      DisbursementService disbursementService,
      LoanApplicationService loanApplicationService,
      AuthService authService) {
    this.disbursementService = disbursementService;
    this.loanApplicationService = loanApplicationService;
    this.authService = authService;
  }

  @GetMapping("/pending")
  @PreAuthorize(
      "@permissionEvaluator.hasPermission(authentication, 'LOAN_DISBURSE') or"
          + " hasRole('BACK_OFFICE')")
  @Operation(
      summary = "Get pending disbursements",
      description = "BACK_OFFICE - Get approved loans pending disbursement")
  public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getPendingDisbursements() {
    List<LoanApplicationResponse> loans = loanApplicationService.getApprovedLoans();

    ApiResponse<List<LoanApplicationResponse>> response =
        ApiResponse.<List<LoanApplicationResponse>>builder()
            .success(true)
            .message("Pending disbursements retrieved successfully")
            .data(loans)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{loanId}")
  @PreAuthorize(
      "@permissionEvaluator.hasPermission(authentication, 'LOAN_DISBURSE') or"
          + " hasRole('BACK_OFFICE')")
  @Operation(
      summary = "Process disbursement",
      description = "BACK_OFFICE - Process loan disbursement")
  public ResponseEntity<ApiResponse<DisbursementResponse>> processDisbursement(
      @PathVariable Long loanId) {
    CustomUserDetails currentUser = authService.getCurrentUser();
    DisbursementResponse disbursement =
        disbursementService.processDisbursement(loanId, currentUser.getId());

    ApiResponse<DisbursementResponse> response =
        ApiResponse.<DisbursementResponse>builder()
            .success(true)
            .message("Disbursement processed successfully")
            .data(disbursement)
            .code(HttpStatus.CREATED.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize(
      "@permissionEvaluator.hasPermission(authentication, 'LOAN_DISBURSE') or"
          + " hasRole('BACK_OFFICE')")
  @Operation(summary = "Get all disbursements", description = "BACK_OFFICE - Get all disbursements")
  public ResponseEntity<ApiResponse<List<DisbursementResponse>>> getAllDisbursements() {
    List<DisbursementResponse> disbursements = disbursementService.getAllDisbursements();

    ApiResponse<List<DisbursementResponse>> response =
        ApiResponse.<List<DisbursementResponse>>builder()
            .success(true)
            .message("Disbursements retrieved successfully")
            .data(disbursements)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/loan/{loanId}")
  @PreAuthorize("hasAnyRole('BACK_OFFICE', 'SUPER_ADMIN')")
  @Operation(
      summary = "Get disbursement by loan ID",
      description = "Get disbursement details for a specific loan")
  public ResponseEntity<ApiResponse<DisbursementResponse>> getDisbursementByLoanId(
      @PathVariable Long loanId) {
    DisbursementResponse disbursement = disbursementService.getDisbursementByLoanId(loanId);

    ApiResponse<DisbursementResponse> response =
        ApiResponse.<DisbursementResponse>builder()
            .success(true)
            .message("Disbursement retrieved successfully")
            .data(disbursement)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }
}
