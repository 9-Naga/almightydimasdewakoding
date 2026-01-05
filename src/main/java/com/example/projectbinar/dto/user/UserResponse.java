package com.example.projectbinar.dto.user;

import java.time.Instant;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private String fullname;
  private String phone;
  private Boolean isActive;
  private Instant createdAt;
  private Set<String> roles;
}
