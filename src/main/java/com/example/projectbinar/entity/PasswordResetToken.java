package com.example.projectbinar.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expiry_date", nullable = false)
  private Instant expiryDate;

  @Column(name = "used")
  @Builder.Default
  private Boolean used = false;

  @PrePersist
  public void generateToken() {
    if (this.token == null) {
      this.token = UUID.randomUUID().toString();
    }
  }

  public boolean isExpired() {
    return Instant.now().isAfter(this.expiryDate);
  }
}
