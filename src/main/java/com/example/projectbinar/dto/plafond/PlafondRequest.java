package com.example.projectbinar.dto.plafond;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Minimum amount is required")
    @Positive(message = "Minimum amount must be positive")
    private BigDecimal minAmount;
    
    @NotNull(message = "Maximum amount is required")
    @Positive(message = "Maximum amount must be positive")
    private BigDecimal maxAmount;
    
    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be positive")
    private BigDecimal interestRate;
    
    @NotNull(message = "Tenor month is required")
    @Positive(message = "Tenor month must be positive")
    private Integer tenorMonth;
    
    private Boolean isActive = true;
}
