package com.example.projectbinar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(unique = true)
  private String email;

  @Column(name = "password_hash")
  @JsonIgnore
  private String passwordHash;

  private String fullname;

  private String phone;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "created_at")
  private Instant createdAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
    if (this.isActive == null) {
      this.isActive = true;
    }
  }
}
