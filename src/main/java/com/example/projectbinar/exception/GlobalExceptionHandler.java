package com.example.projectbinar.exception;

import com.example.projectbinar.base.ApiResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .code(HttpStatus.NOT_FOUND.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .code(HttpStatus.BAD_REQUEST.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .code(HttpStatus.UNAUTHORIZED.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message("Invalid username or password")
            .code(HttpStatus.UNAUTHORIZED.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message("Authentication failed: " + ex.getMessage())
            .code(HttpStatus.UNAUTHORIZED.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message("Access denied: You don't have permission to access this resource")
            .code(HttpStatus.FORBIDDEN.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ApiResponse<Map<String, String>> response =
        ApiResponse.<Map<String, String>>builder()
            .success(false)
            .message("Validation failed")
            .data(errors)
            .code(HttpStatus.BAD_REQUEST.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
    ApiResponse<Object> response =
        ApiResponse.builder()
            .success(false)
            .message("An unexpected error occurred: " + ex.getMessage())
            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .timestamp(Instant.now())
            .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
