package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.notification.NotificationResponse;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.service.AuthService;
import com.example.projectbinar.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User notification management")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

  private final NotificationService notificationService;
  private final AuthService authService;

  public NotificationController(NotificationService notificationService, AuthService authService) {
    this.notificationService = notificationService;
    this.authService = authService;
  }

  @GetMapping
  @Operation(
      summary = "Get all notifications",
      description = "Get all notifications for current user")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications() {
    CustomUserDetails currentUser = authService.getCurrentUser();
    List<NotificationResponse> notifications =
        notificationService.getNotificationsByUserId(currentUser.getId());

    ApiResponse<List<NotificationResponse>> response =
        ApiResponse.<List<NotificationResponse>>builder()
            .success(true)
            .message("Notifications retrieved successfully")
            .data(notifications)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/unread")
  @Operation(
      summary = "Get unread notifications",
      description = "Get unread notifications for current user")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications() {
    CustomUserDetails currentUser = authService.getCurrentUser();
    List<NotificationResponse> notifications =
        notificationService.getUnreadNotifications(currentUser.getId());

    ApiResponse<List<NotificationResponse>> response =
        ApiResponse.<List<NotificationResponse>>builder()
            .success(true)
            .message("Unread notifications retrieved successfully")
            .data(notifications)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/count")
  @Operation(summary = "Get unread count", description = "Get count of unread notifications")
  public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
    CustomUserDetails currentUser = authService.getCurrentUser();
    long count = notificationService.getUnreadCount(currentUser.getId());

    ApiResponse<Long> response =
        ApiResponse.<Long>builder()
            .success(true)
            .message("Unread count retrieved successfully")
            .data(count)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}/read")
  @Operation(summary = "Mark as read", description = "Mark a notification as read")
  public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
    NotificationResponse notification = notificationService.markAsRead(id);

    ApiResponse<NotificationResponse> response =
        ApiResponse.<NotificationResponse>builder()
            .success(true)
            .message("Notification marked as read")
            .data(notification)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PutMapping("/read-all")
  @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
    CustomUserDetails currentUser = authService.getCurrentUser();
    notificationService.markAllAsRead(currentUser.getId());

    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .success(true)
            .message("All notifications marked as read")
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }
}
