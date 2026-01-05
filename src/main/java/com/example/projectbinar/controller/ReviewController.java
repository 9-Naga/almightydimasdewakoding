package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.loan.LoanApplicationResponse;
import com.example.projectbinar.dto.loan.LoanReviewRequest;
import com.example.projectbinar.dto.loan.LoanReviewResponse;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.service.AuthService;
import com.example.projectbinar.service.LoanApplicationService;
import com.example.projectbinar.service.LoanReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Loan Review", description = "Loan review operations for MARKETING role")
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewController {

  private final LoanReviewService loanReviewService;
  private final LoanApplicationService loanApplicationService;
  private final AuthService authService;

  public ReviewController(
      LoanReviewService loanReviewService,
      LoanApplicationService loanApplicationService,
      AuthService authService) {
    this.loanReviewService = loanReviewService;
    this.loanApplicationService = loanApplicationService;
    this.authService = authService;
  }

  @GetMapping("/pending")
  @PreAuthorize(
      "@permissionEvaluator.hasPermission(authentication, 'LOAN_REVIEW') or hasRole('MARKETING')")
  @Operation(
      summary = "Get pending reviews",
      description = "MARKETING - Get loan applications pending review")
  public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getPendingReviews() {
    List<LoanApplicationResponse> loans = loanApplicationService.getPendingReviewLoans();

    ApiResponse<List<LoanApplicationResponse>> response =
        ApiResponse.<List<LoanApplicationResponse>>builder()
            .success(true)
            .message("Pending reviews retrieved successfully")
            .data(loans)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{loanId}")
  @PreAuthorize(
      "@permissionEvaluator.hasPermission(authentication, 'LOAN_REVIEW') or hasRole('MARKETING')")
  @Operation(
      summary = "Submit review",
      description = "MARKETING - Submit review for a loan application")
  public ResponseEntity<ApiResponse<LoanReviewResponse>> submitReview(
      @PathVariable Long loanId, @Valid @RequestBody LoanReviewRequest request) {
    CustomUserDetails currentUser = authService.getCurrentUser();
    LoanReviewResponse review =
        loanReviewService.submitReview(loanId, currentUser.getId(), request);

    ApiResponse<LoanReviewResponse> response =
        ApiResponse.<LoanReviewResponse>builder()
            .success(true)
            .message("Review submitted successfully")
            .data(review)
            .code(HttpStatus.CREATED.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/my-reviews")
  @PreAuthorize(
      "@permissionEvaluator.hasPermission(authentication, 'LOAN_REVIEW') or hasRole('MARKETING')")
  @Operation(
      summary = "Get my reviews",
      description = "MARKETING - Get reviews submitted by current user")
  public ResponseEntity<ApiResponse<List<LoanReviewResponse>>> getMyReviews() {
    CustomUserDetails currentUser = authService.getCurrentUser();
    List<LoanReviewResponse> reviews = loanReviewService.getReviewsByReviewer(currentUser.getId());

    ApiResponse<List<LoanReviewResponse>> response =
        ApiResponse.<List<LoanReviewResponse>>builder()
            .success(true)
            .message("Reviews retrieved successfully")
            .data(reviews)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/loan/{loanId}")
  @PreAuthorize("hasAnyRole('MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE', 'SUPER_ADMIN')")
  @Operation(
      summary = "Get review by loan ID",
      description = "Get review details for a specific loan")
  public ResponseEntity<ApiResponse<LoanReviewResponse>> getReviewByLoanId(
      @PathVariable Long loanId) {
    LoanReviewResponse review = loanReviewService.getReviewByLoanId(loanId);

    ApiResponse<LoanReviewResponse> response =
        ApiResponse.<LoanReviewResponse>builder()
            .success(true)
            .message("Review retrieved successfully")
            .data(review)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }
}
