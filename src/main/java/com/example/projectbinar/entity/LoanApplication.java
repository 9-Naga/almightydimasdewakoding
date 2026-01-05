package com.example.projectbinar.entity;

import com.example.projectbinar.enums.LoanStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "customer_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private CustomerProfile customer;

  @ManyToOne
  @JoinColumn(name = "plafond_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Plafond plafond;

  @Column(precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  @Builder.Default
  private LoanStatus status = LoanStatus.SUBMITTED;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
    this.updatedAt = Instant.now();
    if (this.status == null) {
      this.status = LoanStatus.SUBMITTED;
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }
}
