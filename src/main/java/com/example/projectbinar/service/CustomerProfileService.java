package com.example.projectbinar.service;

import com.example.projectbinar.dto.profile.CustomerProfileRequest;
import com.example.projectbinar.dto.profile.CustomerProfileResponse;
import com.example.projectbinar.entity.CustomerProfile;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.CustomerProfileRepository;
import com.example.projectbinar.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerProfileService {

  private final CustomerProfileRepository customerProfileRepository;
  private final UserRepository userRepository;

  public CustomerProfileService(
      CustomerProfileRepository customerProfileRepository, UserRepository userRepository) {
    this.customerProfileRepository = customerProfileRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public CustomerProfileResponse createOrUpdateProfile(
      Long userId, CustomerProfileRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    CustomerProfile profile =
        customerProfileRepository
            .findByUserId(userId)
            .orElse(CustomerProfile.builder().user(user).build());

    // Check if identity number is unique (if new or changed)
    if (request.getIdentityNumber() != null) {
      boolean exists =
          customerProfileRepository.existsByIdentityNumber(request.getIdentityNumber());
      if (exists
          && (profile.getId() == null
              || !request.getIdentityNumber().equals(profile.getIdentityNumber()))) {
        throw new BadRequestException("Identity number already registered");
      }
    }

    // Update profile fields
    profile.setFullName(request.getFullName());
    profile.setAddress(request.getAddress());
    profile.setIdentityNumber(request.getIdentityNumber());
    System.out.println("DEBUG: tanggalLahir from request = " + request.getTanggalLahir());
    profile.setTanggalLahir(request.getTanggalLahir());
    profile.setBankName(request.getBankName());
    profile.setBankAccountNumber(request.getBankAccountNumber());
    profile.setBankAccountHolderName(request.getBankAccountHolderName());

    // Handle KTP upload (Base64 encoded)
    if (request.getUploadKtp() != null && !request.getUploadKtp().isEmpty()) {
      profile.setUploadKtp(request.getUploadKtp());
    }

    CustomerProfile savedProfile = customerProfileRepository.save(profile);
    return mapToResponse(savedProfile);
  }

  public CustomerProfileResponse getProfileByUserId(Long userId) {
    CustomerProfile profile =
        customerProfileRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Customer profile not found for user: " + userId));
    return mapToResponse(profile);
  }

  public CustomerProfile getProfileEntityByUserId(Long userId) {
    return customerProfileRepository
        .findByUserId(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Customer profile not found for user: " + userId));
  }

  public boolean hasCompleteProfile(Long userId) {
    return customerProfileRepository
        .findByUserId(userId)
        .map(
            profile ->
                profile.getFullName() != null
                    && profile.getIdentityNumber() != null
                    && profile.getTanggalLahir() != null
                    && profile.getBankAccountNumber() != null
                    && profile.getUploadKtp() != null)
        .orElse(false);
  }

  public String getKtpImage(Long userId) {
    CustomerProfile profile =
        customerProfileRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
    return profile.getUploadKtp();
  }

  public String uploadKtp(Long userId, MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("File is empty");
    }

    try {
      // Create uploads directory if not exists
      String uploadDir = "uploads/ktp/";
      Path uploadPath = Paths.get(uploadDir);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // Generate unique filename
      String originalFilename = file.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }
      String filename = "ktp_" + userId + "_" + UUID.randomUUID().toString() + extension;

      // Save file
      Path filePath = uploadPath.resolve(filename);
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      // Update database
      CustomerProfile profile =
          customerProfileRepository
              .findByUserId(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

      String fileUrl = "uploads/ktp/" + filename; // Relative path/URL
      profile.setUploadKtp(fileUrl);
      customerProfileRepository.save(profile);

      return fileUrl;

    } catch (IOException e) {
      throw new RuntimeException("Failed to store file", e);
    }
  }

  private CustomerProfileResponse mapToResponse(CustomerProfile profile) {
    String ktpUrl = profile.getUploadKtp();
    return CustomerProfileResponse.builder()
        .id(profile.getId())
        .userId(profile.getUser().getId())
        .username(profile.getUser().getUsername())
        .email(profile.getUser().getEmail())
        .fullName(profile.getFullName())
        .address(profile.getAddress())
        .identityNumber(profile.getIdentityNumber())
        .tanggalLahir(profile.getTanggalLahir())
        .bankName(profile.getBankName())
        .bankAccountNumber(profile.getBankAccountNumber())
        .bankAccountHolderName(profile.getBankAccountHolderName())
        .ktpUrl(ktpUrl)
        .hasKtpUploaded(ktpUrl != null && !ktpUrl.isEmpty())
        .createdAt(profile.getCreatedAt())
        .build();
  }
}
