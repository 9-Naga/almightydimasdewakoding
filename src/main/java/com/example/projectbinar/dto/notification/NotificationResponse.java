package com.example.projectbinar.dto.notification;

import com.example.projectbinar.enums.NotificationChannel;
import com.example.projectbinar.enums.NotificationType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
  private Long id;
  private Long userId;
  private Long loanApplicationId;
  private NotificationType type;
  private NotificationChannel channel;
  private String message;
  private Boolean isRead;
  private Instant createdAt;
}
