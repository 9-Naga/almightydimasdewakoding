package com.example.projectbinar.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${app.firebase.config-path:firebase-service-account.json}")
  private String firebaseConfigPath;

  @Bean
  public FirebaseApp firebaseApp() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        InputStream serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        logger.info("Firebase application has been initialized");
        return app;
      }
      return FirebaseApp.getInstance();
    } catch (IOException e) {
      logger.error("Failed to initialize Firebase: {}", e.getMessage());
      // Return null or throw exception based on strictness requirement.
      // For now, we log error but allow app to start (notification will fail later)
      return null;
    }
  }
}
