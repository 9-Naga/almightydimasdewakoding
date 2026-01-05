package com.example.projectbinar.dto.profile;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {
  private Long id;
  private Long userId;
  private String username;
  private String email;
  private String fullName;
  private String address;
  private String identityNumber;
  private String bankName;
  private String bankAccountNumber;
  private String bankAccountHolderName;
  private boolean hasKtpUploaded;
  private Instant createdAt;
}
