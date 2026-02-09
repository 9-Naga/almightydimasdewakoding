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

  @Value("${app.firebase.config-path:classpath:firebase-service-account.json}")
  private String firebaseConfigPath;

  private final org.springframework.core.io.ResourceLoader resourceLoader;

  public FirebaseConfig(org.springframework.core.io.ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Bean
  public FirebaseApp firebaseApp() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        org.springframework.core.io.Resource resource = resourceLoader.getResource(firebaseConfigPath);
        
        if (!resource.exists()) {
            logger.warn("Firebase config file not found at: {}", firebaseConfigPath);
            return null;
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options =
                FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
    
            FirebaseApp app = FirebaseApp.initializeApp(options);
            logger.info("Firebase application has been initialized");
            return app;
        }
      }
      return FirebaseApp.getInstance();
    } catch (IOException e) {
      logger.error("Failed to initialize Firebase: {}", e.getMessage());
      return null;
    }
  }
}
