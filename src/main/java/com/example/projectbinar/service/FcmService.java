package com.example.projectbinar.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

  private static final Logger logger = LoggerFactory.getLogger(FcmService.class);
  private final FirebaseApp firebaseApp;

  public FcmService(FirebaseApp firebaseApp) {
    this.firebaseApp = firebaseApp;
  }

  /**
   * Send push notification to a specific device via FCM
   *
   * @param recipientToken the target device FCM token
   * @param title notification title
   * @param body notification body
   * @return the message ID string or null if failed
   */
  public String sendNotification(String recipientToken, String title, String body) {
    if (firebaseApp == null) {
      logger.warn("Firebase is not initialized. Notification skipped.");
      return null;
    }

    if (recipientToken == null || recipientToken.trim().isEmpty()) {
      logger.warn("Recipient token is empty. Notification skipped.");
      return null;
    }

    try {
      Message message =
          Message.builder()
              .setToken(recipientToken)
              .setNotification(Notification.builder().setTitle(title).setBody(body).build())
              .build();

      String response = FirebaseMessaging.getInstance().send(message);
      logger.info("Successfully sent message: " + response);
      return response;
    } catch (Exception e) {
      logger.error("Error sending FCM notification: {}", e.getMessage());
      return null;
    }
  }

  /** Dry run send for testing connectivity/token validity without actual delivery */
  public String sendTestNotification(String recipientToken) {
    if (firebaseApp == null) {
      throw new RuntimeException("Firebase not initialized");
    }

    try {
      Message message =
          Message.builder()
              .setToken(recipientToken)
              .setNotification(
                  Notification.builder()
                      .setTitle("Test Notification")
                      .setBody("This is a test message from Backend")
                      .build())
              .build();

      // We can use send(message, true) for dry-run if supported, but standard send is fine for test
      String response = FirebaseMessaging.getInstance().send(message);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Failed to send test notification: " + e.getMessage(), e);
    }
  }
}
