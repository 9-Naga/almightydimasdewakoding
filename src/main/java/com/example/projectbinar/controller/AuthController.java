package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.auth.*;
import com.example.projectbinar.dto.user.UserResponse;
import com.example.projectbinar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public authentication endpoints")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @Operation(
      summary = "Register new user",
      description = "Register a new user account with USER role")
  public ResponseEntity<ApiResponse<UserResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    UserResponse user = authService.register(request);

    ApiResponse<UserResponse> response =
        ApiResponse.<UserResponse>builder()
            .success(true)
            .message("User registered successfully")
            .data(user)
            .code(HttpStatus.CREATED.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate user and get JWT token")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    System.out.println("fcm_token : " + request.getFcmToken());
    LoginResponse loginResponse = authService.login(request);

    ApiResponse<LoginResponse> response =
        ApiResponse.<LoginResponse>builder()
            .success(true)
            .message("Login successful")
            .data(loginResponse)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout", description = "Invalidate current JWT token")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    authService.logout(token);

    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .success(true)
            .message("Logout successful")
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/forgot-password")
  @Operation(
      summary = "Forgot password",
      description = "Request password reset - sends email with reset link")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);

    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .success(true)
            .message(
                "Password reset link has been sent to your email address. Please check your inbox.")
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset password", description = "Reset password using token")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);

    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .success(true)
            .message("Password reset successful")
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }
}
