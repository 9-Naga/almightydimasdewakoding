package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.DisbursementStatus;
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
public class DisbursementResponse {
  private Long id;
  private Long loanApplicationId;
  private BigDecimal disbursementAmount;
  private Instant disbursementDate;
  private DisbursementStatus status;
  private Long processedById;
  private String processedByName;
}
