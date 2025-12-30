package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.profile.CustomerProfileRequest;
import com.example.projectbinar.dto.profile.CustomerProfileResponse;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.service.AuthService;
import com.example.projectbinar.service.CustomerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Customer Profile", description = "Customer profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;
    private final AuthService authService;

    public CustomerProfileController(CustomerProfileService customerProfileService, AuthService authService) {
        this.customerProfileService = customerProfileService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current user profile", description = "Get profile of the logged-in user")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getMyProfile() {
        CustomUserDetails currentUser = authService.getCurrentUser();
        CustomerProfileResponse profile = customerProfileService.getProfileByUserId(currentUser.getId());
        
        ApiResponse<CustomerProfileResponse> response = ApiResponse.<CustomerProfileResponse>builder()
                .success(true)
                .message("Profile retrieved successfully")
                .data(profile)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update profile", description = "Create or update profile for the logged-in user")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(@Valid @RequestBody CustomerProfileRequest request) {
        CustomUserDetails currentUser = authService.getCurrentUser();
        CustomerProfileResponse profile = customerProfileService.createOrUpdateProfile(currentUser.getId(), request);
        
        ApiResponse<CustomerProfileResponse> response = ApiResponse.<CustomerProfileResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(profile)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ktp")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get KTP image", description = "Get Base64 encoded KTP image")
    public ResponseEntity<ApiResponse<String>> getKtpImage() {
        CustomUserDetails currentUser = authService.getCurrentUser();
        String ktpBase64 = customerProfileService.getKtpImage(currentUser.getId());
        
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("KTP image retrieved successfully")
                .data(ktpBase64)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Check profile completion status", description = "Check if profile is complete for loan application")
    public ResponseEntity<ApiResponse<Boolean>> checkProfileStatus() {
        CustomUserDetails currentUser = authService.getCurrentUser();
        boolean isComplete = customerProfileService.hasCompleteProfile(currentUser.getId());
        
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message(isComplete ? "Profile is complete" : "Profile is incomplete. Please complete your profile including KTP upload.")
                .data(isComplete)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
