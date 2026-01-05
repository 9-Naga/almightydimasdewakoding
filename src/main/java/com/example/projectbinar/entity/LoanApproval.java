package com.example.projectbinar.entity;

import com.example.projectbinar.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_approval")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApproval implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "loan_application_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private LoanApplication loanApplication;

  @ManyToOne
  @JoinColumn(name = "approver_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash", "roles"})
  private User approver;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", length = 20)
  private ApprovalStatus approvalStatus;

  @Column(name = "approval_note", columnDefinition = "TEXT")
  private String approvalNote;

  @Column(name = "approval_date")
  private Instant approvalDate;

  @PrePersist
  public void prePersist() {
    if (this.approvalDate == null) {
      this.approvalDate = Instant.now();
    }
  }
}
