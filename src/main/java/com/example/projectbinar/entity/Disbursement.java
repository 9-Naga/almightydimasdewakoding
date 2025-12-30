package com.example.projectbinar.entity;

import com.example.projectbinar.enums.DisbursementStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "disbursement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Disbursement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "loan_application_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LoanApplication loanApplication;

    @Column(name = "disbursement_amount", precision = 19, scale = 2)
    private BigDecimal disbursementAmount;

    @Column(name = "disbursement_date")
    private Instant disbursementDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private DisbursementStatus status = DisbursementStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "processed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash", "roles"})
    private User processedBy;

    @PrePersist
    public void prePersist() {
        if (this.disbursementDate == null) {
            this.disbursementDate = Instant.now();
        }
    }
}
