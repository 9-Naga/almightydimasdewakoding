package com.example.projectbinar.service;

import com.example.projectbinar.dto.auth.GoogleLoginRequest;
import com.example.projectbinar.dto.auth.LoginResponse;
import com.example.projectbinar.entity.Role;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.RoleRepository;
import com.example.projectbinar.repository.UserRepository;
import com.example.projectbinar.security.JwtUtils;
import com.example.projectbinar.security.RedisTokenService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirebaseAuthService {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

  private final FirebaseApp firebaseApp;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtUtils jwtUtils;
  private final RedisTokenService redisTokenService;

  public FirebaseAuthService(
      FirebaseApp firebaseApp,
      UserRepository userRepository,
      RoleRepository roleRepository,
      JwtUtils jwtUtils,
      RedisTokenService redisTokenService) {
    this.firebaseApp = firebaseApp;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.jwtUtils = jwtUtils;
    this.redisTokenService = redisTokenService;
  }

  /**
   * Verify Firebase ID Token and login/register user
   *
   * @param request GoogleLoginRequest containing Firebase ID Token
   * @return LoginResponse with JWT token
   */
  @Transactional
  public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
    if (firebaseApp == null) {
      throw new BadRequestException("Firebase is not configured. Please contact administrator.");
    }

    // Verify Firebase ID Token
    FirebaseToken decodedToken = verifyIdToken(request.getIdToken());

    // Get or create user
    User user = getOrCreateUser(decodedToken);

    // Update FCM Token if provided
    if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
      user.setFcmToken(request.getFcmToken());
      userRepository.save(user);
      logger.info("FCM Token updated for Google user: {}", user.getUsername());
    }

    // Generate JWT Token
    String authorities =
        user.getRoles().stream()
            .flatMap(
                role -> {
                  Set<String> auths = new HashSet<>();
                  auths.add("ROLE_" + role.getName());
                  if (role.getPermissions() != null) {
                    role.getPermissions().forEach(p -> auths.add(p.getName()));
                  }
                  return auths.stream();
                })
            .collect(Collectors.joining(","));

    String jwt = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getId(), authorities);

    // Store token in Redis
    redisTokenService.storeToken(user.getUsername(), jwt, jwtUtils.getExpirationMs());

    // Build response
    Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    Set<String> permissions =
        user.getRoles().stream()
            .filter(role -> role.getPermissions() != null)
            .flatMap(role -> role.getPermissions().stream())
            .map(p -> p.getName())
            .collect(Collectors.toSet());

    logger.info("Google login successful for user: {}", user.getUsername());

    return LoginResponse.builder()
        .accessToken(jwt)
        .tokenType("Bearer")
        .expiresIn(jwtUtils.getExpirationMs() / 1000)
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .fullname(user.getFullname())
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  /**
   * Verify Firebase ID Token
   *
   * @param idToken Firebase ID Token from client
   * @return Decoded FirebaseToken
   */
  private FirebaseToken verifyIdToken(String idToken) {
    try {
      return FirebaseAuth.getInstance().verifyIdToken(idToken);
    } catch (FirebaseAuthException e) {
      logger.error("Firebase token verification failed: {}", e.getMessage());
      throw new BadRequestException("Invalid Firebase ID Token: " + e.getMessage());
    }
  }

  /**
   * Get existing user by email or create new user
   *
   * @param firebaseToken Decoded Firebase token
   * @return User entity
   */
  private User getOrCreateUser(FirebaseToken firebaseToken) {
    String email = firebaseToken.getEmail();
    String name = firebaseToken.getName();

    if (email == null || email.isEmpty()) {
      throw new BadRequestException(
          "Email is required for Google Sign-In. Please ensure your Google account has an email.");
    }

    // Try to find user by email
    User user = userRepository.findByEmail(email).orElse(null);

    if (user != null) {
      // Update fullname if not set and Google provides it
      if ((user.getFullname() == null || user.getFullname().isEmpty()) && name != null) {
        user.setFullname(name);
        user = userRepository.save(user);
        logger.info("Updated fullname for Google user: {}", user.getUsername());
      }
      return user;
    }

    // Create new user from Google account
    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    // Generate username from email
    String username = email.split("@")[0];

    // Ensure username is unique
    String baseUsername = username;
    int counter = 1;
    while (userRepository.findByUsername(username).isPresent()) {
      username = baseUsername + counter;
      counter++;
    }

    user =
        User.builder()
            .username(username)
            .email(email)
            .fullname(name)
            .isActive(true)
            .createdAt(Instant.now())
            .roles(roles)
            .build();

    user = userRepository.save(user);
    logger.info("Created new user from Google account: {}", user.getUsername());

    return user;
  }
}
