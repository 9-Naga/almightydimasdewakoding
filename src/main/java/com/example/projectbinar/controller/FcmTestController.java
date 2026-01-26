package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "FCM Test", description = "Test Firebase Cloud Messaging")
public class FcmTestController {

  private final FcmService fcmService;

  public FcmTestController(FcmService fcmService) {
    this.fcmService = fcmService;
  }

  @PostMapping("/test-fcm")
  @Operation(
      summary = "Test FCM Connection",
      description = "Send a test notification to a specific token")
  public ResponseEntity<ApiResponse<String>> testFcmConnection(@RequestParam String fcmToken) {
    try {
      String response = fcmService.sendTestNotification(fcmToken);

      ApiResponse<String> apiResponse =
          ApiResponse.<String>builder()
              .success(true)
              .message("Test notification sent successfully")
              .data("Message ID: " + response)
              .code(HttpStatus.OK.value())
              .timestamp(Instant.now())
              .build();

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      ApiResponse<String> apiResponse =
          ApiResponse.<String>builder()
              .success(false)
              .message("Failed to send test notification: " + e.getMessage())
              .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .timestamp(Instant.now())
              .build();

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }
  }
}
