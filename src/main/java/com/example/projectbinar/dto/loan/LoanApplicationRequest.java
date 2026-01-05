package com.example.projectbinar.dto.loan;

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
public class LoanApplicationRequest {

  @NotNull(message = "Plafond ID is required") private Long plafondId;

  @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") private BigDecimal amount;
}
