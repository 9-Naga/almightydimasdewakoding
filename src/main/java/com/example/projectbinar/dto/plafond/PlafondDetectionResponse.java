package com.example.projectbinar.dto.plafond;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondDetectionResponse {

  private boolean found;
  private Long plafondId;
  private String plafondName;
  private BigDecimal minAmount;
  private BigDecimal maxAmount;
  private BigDecimal baseInterestRate;
  private Integer maxTenorMonth;
  private List<Integer> availableTenors;
  private String message;
}
