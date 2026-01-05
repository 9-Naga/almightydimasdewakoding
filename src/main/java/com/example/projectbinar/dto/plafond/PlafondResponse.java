package com.example.projectbinar.dto.plafond;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondResponse {
  private Long id;
  private String name;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private BigDecimal interestRate;
  private Integer tenorMonth;
  private Boolean isActive;
}
