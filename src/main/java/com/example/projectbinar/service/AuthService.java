package com.example.projectbinar.service;

import com.example.projectbinar.dto.auth.*;
import com.example.projectbinar.dto.user.UserResponse;
import com.example.projectbinar.entity.PasswordResetToken;
import com.example.projectbinar.entity.Role;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.exception.UnauthorizedException;
import com.example.projectbinar.repository.PasswordResetTokenRepository;
import com.example.projectbinar.repository.RoleRepository;
import com.example.projectbinar.repository.UserRepository;
import com.example.projectbinar.security.CustomUserDetails;
import com.example.projectbinar.security.JwtUtils;
import com.example.projectbinar.security.RedisTokenService;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final RedisTokenService redisTokenService;
  private final EmailService emailService;

  @Value("${app.password-reset.expiration:3600000}")
  private Long passwordResetExpiration;

  public AuthService(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtUtils jwtUtils,
      RedisTokenService redisTokenService,
      EmailService emailService) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtils = jwtUtils;
    this.redisTokenService = redisTokenService;
    this.emailService = emailService;
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    // Check if username exists
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new BadRequestException("Username is already taken");
    }

    // Get USER role
    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    // Create new user
    User user =
        User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullname(request.getFullname())
            .phone(request.getPhone())
            .isActive(true)
            .createdAt(Instant.now())
            .roles(roles)
            .build();

    User savedUser = userRepository.save(user);

    return mapToUserResponse(savedUser);
  }

  public LoginResponse login(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateToken(authentication);

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    // Store token in Redis
    redisTokenService.storeToken(userDetails.getUsername(), jwt, jwtUtils.getExpirationMs());

    Set<String> roles =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5)) // Remove ROLE_ prefix
            .collect(Collectors.toSet());

    Set<String> permissions =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> !auth.startsWith("ROLE_"))
            .collect(Collectors.toSet());

    return LoginResponse.builder()
        .accessToken(jwt)
        .tokenType("Bearer")
        .expiresIn(jwtUtils.getExpirationMs() / 1000) // Convert to seconds
        .userId(userDetails.getId())
        .username(userDetails.getUsername())
        .email(userDetails.getEmail())
        .fullname(userDetails.getFullname())
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  public void logout(String token) {
    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    if (jwtUtils.validateToken(token)) {
      String username = jwtUtils.getUsernameFromToken(token);

      // Blacklist the token
      redisTokenService.blacklistToken(token, jwtUtils.getExpirationMs());

      // Remove stored token
      redisTokenService.removeToken(username);
    }
  }

  @Transactional
  public void forgotPassword(ForgotPasswordRequest request) {
    User user =
        userRepository.findAll().stream()
            .filter(u -> u.getEmail() != null && u.getEmail().equals(request.getEmail()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

    // Delete any existing token
    passwordResetTokenRepository
        .findByUserAndUsedFalse(user)
        .ifPresent(passwordResetTokenRepository::delete);

    // Create new reset token
    PasswordResetToken resetToken =
        PasswordResetToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiryDate(Instant.now().plusMillis(passwordResetExpiration))
            .used(false)
            .build();

    passwordResetTokenRepository.save(resetToken);

    // Send email with reset link
    try {
      emailService.sendPasswordResetEmail(
          user.getEmail(),
          resetToken.getToken(),
          user.getFullname() != null ? user.getFullname() : user.getUsername());
      logger.info("Password reset email sent to: {}", user.getEmail());
    } catch (Exception e) {
      logger.error("Failed to send password reset email: {}", e.getMessage());
      throw new BadRequestException("Failed to send password reset email. Please try again later.");
    }
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(request.getToken())
            .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

    if (resetToken.getUsed()) {
      throw new BadRequestException("Reset token has already been used");
    }

    if (resetToken.isExpired()) {
      throw new BadRequestException("Reset token has expired");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Mark token as used
    resetToken.setUsed(true);
    passwordResetTokenRepository.save(resetToken);
  }

  public CustomUserDetails getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("User not authenticated");
    }
    return (CustomUserDetails) authentication.getPrincipal();
  }

  private UserResponse mapToUserResponse(User user) {
    Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .fullname(user.getFullname())
        .phone(user.getPhone())
        .isActive(user.getIsActive())
        .createdAt(user.getCreatedAt())
        .roles(roleNames)
        .build();
  }
}
