package com.example.projectbinar.dto.loan;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class LoanApplicationRequest {

  @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") private BigDecimal amount;

  @NotNull(message = "Tenor is required") @Min(value = 1, message = "Tenor must be at least 1 month")
  @Max(value = 60, message = "Tenor cannot exceed 60 months")
  private Integer tenorMonth;

  @NotNull(message = "Latitude is required") @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  private BigDecimal latitude;

  @NotNull(message = "Longitude is required") @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  private BigDecimal longitude;
}
