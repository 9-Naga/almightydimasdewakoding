package com.example.projectbinar.dto.loan;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSimulationResponse {

  // Product details
  private Long plafondId;
  private String plafondName;

  // Loan details
  private BigDecimal amount;
  private Integer tenorMonth;
  private Integer maxTenorMonth;

  // Interest calculation
  private BigDecimal baseInterestRate;
  private BigDecimal actualInterestRate;

  // Payment calculation
  private BigDecimal totalInterest;
  private BigDecimal totalPayment;
  private BigDecimal monthlyInstallment;

  // Additional info
  private String message;
}
