package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.profile.CustomerProfileResponse;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.service.CustomerProfileService;
import com.example.projectbinar.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User management endpoints for SUPER_ADMIN")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserController {

  @Autowired private UserService userService;
  @Autowired private CustomerProfileService customerProfileService;

  @GetMapping
  @Operation(summary = "Get all users", description = "SUPER_ADMIN - Get all users")
  public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
    List<User> users = userService.getAllUsers();

    ApiResponse<List<User>> response =
        ApiResponse.<List<User>>builder()
            .message("Data user berhasil diambil")
            .success(true)
            .data(users)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "SUPER_ADMIN - Get user by ID")
  public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
    return userService
        .getUserById(id)
        .map(
            user -> {
              ApiResponse<User> response =
                  ApiResponse.<User>builder()
                      .message("Data user berhasil diambil")
                      .success(true)
                      .data(user)
                      .code(HttpStatus.OK.value())
                      .timestamp(Instant.now())
                      .build();
              return ResponseEntity.ok(response);
            })
        .orElseGet(
            () -> {
              ApiResponse<User> response =
                  ApiResponse.<User>builder()
                      .message("User tidak ditemukan")
                      .success(false)
                      .data(null)
                      .code(HttpStatus.NOT_FOUND.value())
                      .timestamp(Instant.now())
                      .build();
              return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            });
  }

  @GetMapping("/{userId}/profile")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING', 'BRANCH_MANAGER', 'BACK_OFFICE')")
  @Operation(
      summary = "Get user profile by ID",
      description = "Admin only - Get customer profile for loan application review")
  public ResponseEntity<ApiResponse<CustomerProfileResponse>> getUserProfile(
      @PathVariable Long userId) {
    CustomerProfileResponse profile = customerProfileService.getProfileByUserId(userId);

    ApiResponse<CustomerProfileResponse> response =
        ApiResponse.<CustomerProfileResponse>builder()
            .success(true)
            .message("User profile retrieved successfully")
            .data(profile)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping
  @Operation(summary = "Create user", description = "SUPER_ADMIN - Create new user")
  public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
    User createdUser = userService.createUser(user);

    ApiResponse<User> response =
        ApiResponse.<User>builder()
            .message("User berhasil dibuat!")
            .success(true)
            .data(createdUser)
            .code(HttpStatus.CREATED.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "SUPER_ADMIN - Update user")
  public ResponseEntity<ApiResponse<User>> updateUser(
      @PathVariable Long id, @RequestBody User userDetails) {
    try {
      User updatedUser = userService.updateUser(id, userDetails);

      ApiResponse<User> response =
          ApiResponse.<User>builder()
              .message("User berhasil diupdate!")
              .success(true)
              .data(updatedUser)
              .code(HttpStatus.OK.value())
              .timestamp(Instant.now())
              .build();

      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      ApiResponse<User> response =
          ApiResponse.<User>builder()
              .message(e.getMessage())
              .success(false)
              .data(null)
              .code(HttpStatus.NOT_FOUND.value())
              .timestamp(Instant.now())
              .build();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user (soft delete)", description = "SUPER_ADMIN - Soft delete user")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    try {
      userService.softDeleteUser(id);

      ApiResponse<Void> response =
          ApiResponse.<Void>builder()
              .message("User berhasil dihapus!")
              .success(true)
              .data(null)
              .code(HttpStatus.OK.value())
              .timestamp(Instant.now())
              .build();

      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      ApiResponse<Void> response =
          ApiResponse.<Void>builder()
              .message(e.getMessage())
              .success(false)
              .data(null)
              .code(HttpStatus.NOT_FOUND.value())
              .timestamp(Instant.now())
              .build();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
  }
}
