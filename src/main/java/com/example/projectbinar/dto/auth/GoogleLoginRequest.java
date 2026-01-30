package com.example.projectbinar.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

  @NotBlank(message = "Firebase ID Token is required")
  private String idToken;

  private String fcmToken; // Optional from mobile client
}
