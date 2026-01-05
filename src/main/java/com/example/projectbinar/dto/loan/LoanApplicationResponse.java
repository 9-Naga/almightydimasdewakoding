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
  private Long id;
  private Long customerId;
  private String customerName;
  private Long plafondId;
  private String plafondName;
  private BigDecimal amount;
  private BigDecimal interestRate;
  private Integer tenorMonth;
  private LoanStatus status;
  private Instant createdAt;
  private Instant updatedAt;
}
