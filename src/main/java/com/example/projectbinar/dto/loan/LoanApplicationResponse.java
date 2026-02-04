package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.LoanStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {

  // Loan identification
  private Long id;

  // User ID (for fetching profile/KTP via /api/users/{userId}/profile)
  private Long userId;

  // Customer details
  private Long customerId;
  private String customerName;

  // Product details
  private Long plafondId;
  private String plafondName;

  // Loan details
  private BigDecimal amount;
  private Integer tenorMonth;
  private Integer maxTenorMonth;

  // Interest details
  private BigDecimal baseInterestRate;
  private BigDecimal actualInterestRate;

  // Payment calculation
  private BigDecimal totalInterest;
  private BigDecimal totalPayment;
  private BigDecimal monthlyInstallment;

  // Status
  private LoanStatus status;
  private Instant createdAt;
  private Instant updatedAt;

  // Location
  private BigDecimal latitude;
  private BigDecimal longitude;
}
