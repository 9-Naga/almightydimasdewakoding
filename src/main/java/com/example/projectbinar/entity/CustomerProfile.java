package com.example.projectbinar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @Column(name = "full_name")
  private String fullName;

  private String address;

  @Column(name = "identity_number")
  private String identityNumber;

  @Column(name = "tanggal_lahir")
  private java.time.LocalDate tanggalLahir;

  // Bank Account Information
  @Column(name = "bank_name")
  private String bankName;

  @Column(name = "bank_account_number")
  private String bankAccountNumber;

  @Column(name = "bank_account_holder_name")
  private String bankAccountHolderName;

  // KTP Upload - stored as Base64 encoded string
  @Column(name = "upload_ktp", columnDefinition = "TEXT")
  @JsonIgnore
  private String uploadKtp;

  @Column(name = "created_at")
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
  }
}
