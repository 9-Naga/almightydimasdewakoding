package com.example.projectbinar.dto.loan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSimulationRequest {

  @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") private BigDecimal amount;

  @NotNull(message = "Tenor is required") @Min(value = 1, message = "Tenor must be at least 1 month")
  @Max(value = 60, message = "Tenor cannot exceed 60 months")
  private Integer tenorMonth;
}
