package com.example.projectbinar.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plafond")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plafond implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(name = "min_amount", precision = 19, scale = 2)
  private BigDecimal minAmount;

  @Column(name = "max_amount", precision = 19, scale = 2)
  private BigDecimal maxAmount;

  @Column(name = "interest_rate", precision = 5, scale = 2)
  private BigDecimal interestRate;

  @Column(name = "tenor_month")
  private Integer tenorMonth;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;
}
