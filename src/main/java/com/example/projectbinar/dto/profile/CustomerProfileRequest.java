package com.example.projectbinar.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileRequest {

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "Identity number (KTP) is required")
  private String identityNumber;

  @com.fasterxml.jackson.annotation.JsonFormat(
      shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd")
  private java.time.LocalDate tanggalLahir;

  // Bank account information
  private String bankName;
  private String bankAccountNumber;
  private String bankAccountHolderName;

  // KTP image as Base64 encoded string
  private String uploadKtp;
}
