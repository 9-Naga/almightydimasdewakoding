package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.loan.LoanApplicationRequest;
import com.example.projectbinar.dto.loan.LoanApplicationResponse;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.service.AuthService;
import com.example.projectbinar.service.LoanApplicationService;
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
@RequestMapping("/api/loans")
@Tag(name = "Loan Application", description = "Loan application management")
@SecurityRequirement(name = "Bearer Authentication")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final AuthService authService;

    public LoanApplicationController(LoanApplicationService loanApplicationService, AuthService authService) {
        this.loanApplicationService = loanApplicationService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Submit loan application", description = "USER role - submit new loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> createLoanApplication(@Valid @RequestBody LoanApplicationRequest request) {
        CustomUserDetails currentUser = authService.getCurrentUser();
        LoanApplicationResponse loan = loanApplicationService.createLoanApplication(currentUser.getId(), request);
        
        ApiResponse<LoanApplicationResponse> response = ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .message("Loan application submitted successfully")
                .data(loan)
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my loan applications", description = "USER role - get own loan applications")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getMyLoans() {
        CustomUserDetails currentUser = authService.getCurrentUser();
        List<LoanApplicationResponse> loans = loanApplicationService.getLoansByUserId(currentUser.getId());
        
        ApiResponse<List<LoanApplicationResponse>> response = ApiResponse.<List<LoanApplicationResponse>>builder()
                .success(true)
                .message("Loan applications retrieved successfully")
                .data(loans)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE', 'SUPER_ADMIN')")
    @Operation(summary = "Get loan application by ID", description = "Get details of a specific loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getLoanById(@PathVariable Long id) {
        LoanApplicationResponse loan = loanApplicationService.getLoanById(id);
        
        ApiResponse<LoanApplicationResponse> response = ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .message("Loan application retrieved successfully")
                .data(loan)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE', 'SUPER_ADMIN')")
    @Operation(summary = "Get all loan applications", description = "Admin roles - get all loan applications")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getAllLoans() {
        List<LoanApplicationResponse> loans = loanApplicationService.getPendingReviewLoans();
        
        ApiResponse<List<LoanApplicationResponse>> response = ApiResponse.<List<LoanApplicationResponse>>builder()
                .success(true)
                .message("Loan applications retrieved successfully")
                .data(loans)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
