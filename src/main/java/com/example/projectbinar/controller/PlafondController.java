package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.plafond.PlafondDetectionResponse;
import com.example.projectbinar.dto.plafond.PlafondRequest;
import com.example.projectbinar.dto.plafond.PlafondResponse;
import com.example.projectbinar.service.PlafondService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plafonds")
@Tag(
    name = "Plafond/Products",
    description =
        "Loan product management with dynamic product detection (public GET, admin for others)")
public class PlafondController {

  private final PlafondService plafondService;

  public PlafondController(PlafondService plafondService) {
    this.plafondService = plafondService;
  }

  @GetMapping
  @Operation(
      summary = "Get all active plafonds",
      description = "Public endpoint - no authentication required")
  public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllActivePlafonds() {
    List<PlafondResponse> plafonds = plafondService.getAllActivePlafonds();

    ApiResponse<List<PlafondResponse>> response =
        ApiResponse.<List<PlafondResponse>>builder()
            .success(true)
            .message("Plafonds retrieved successfully")
            .data(plafonds)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/detect")
  @Operation(
      summary = "Detect product by loan amount",
      description =
          "Public endpoint - Auto-detect which product a loan amount qualifies for. "
              + "Returns product details and available tenor options.")
  public ResponseEntity<ApiResponse<PlafondDetectionResponse>> detectPlafondByAmount(
      @RequestParam BigDecimal amount) {
    PlafondDetectionResponse detection = plafondService.detectPlafondByAmount(amount);

    String message =
        detection.isFound()
            ? "Product found for the requested amount"
            : "No product available for the requested amount";

    ApiResponse<PlafondDetectionResponse> response =
        ApiResponse.<PlafondDetectionResponse>builder()
            .success(detection.isFound())
            .message(message)
            .data(detection)
            .code(detection.isFound() ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(detection.isFound() ? HttpStatus.OK : HttpStatus.NOT_FOUND)
        .body(response);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get plafond by ID",
      description = "Public endpoint - no authentication required")
  public ResponseEntity<ApiResponse<PlafondResponse>> getPlafondById(@PathVariable Long id) {
    PlafondResponse plafond = plafondService.getPlafondById(id);

    ApiResponse<PlafondResponse> response =
        ApiResponse.<PlafondResponse>builder()
            .success(true)
            .message("Plafond retrieved successfully")
            .data(plafond)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/all")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(
      summary = "Get all plafonds including inactive",
      description = "SUPER_ADMIN only - includes inactive plafonds for management purposes")
  public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllPlafonds() {
    List<PlafondResponse> plafonds = plafondService.getAllPlafonds();

    ApiResponse<List<PlafondResponse>> response =
        ApiResponse.<List<PlafondResponse>>builder()
            .success(true)
            .message("All plafonds retrieved successfully")
            .data(plafonds)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(
      summary = "Create new plafond",
      description =
          "SUPER_ADMIN only - Create new product. "
              + "System validates no overlap with existing product amount ranges.")
  public ResponseEntity<ApiResponse<PlafondResponse>> createPlafond(
      @Valid @RequestBody PlafondRequest request) {
    PlafondResponse plafond = plafondService.createPlafond(request);

    ApiResponse<PlafondResponse> response =
        ApiResponse.<PlafondResponse>builder()
            .success(true)
            .message(
                "Plafond '"
                    + plafond.getName()
                    + "' created successfully. "
                    + "It is now available for loan applications.")
            .data(plafond)
            .code(HttpStatus.CREATED.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(
      summary = "Update plafond",
      description =
          "SUPER_ADMIN only - Update product details. "
              + "System validates no overlap with other product amount ranges.")
  public ResponseEntity<ApiResponse<PlafondResponse>> updatePlafond(
      @PathVariable Long id, @Valid @RequestBody PlafondRequest request) {
    PlafondResponse plafond = plafondService.updatePlafond(id, request);

    ApiResponse<PlafondResponse> response =
        ApiResponse.<PlafondResponse>builder()
            .success(true)
            .message("Plafond '" + plafond.getName() + "' updated successfully")
            .data(plafond)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(
      summary = "Deactivate plafond (soft delete)",
      description =
          "SUPER_ADMIN only - Deactivates product by setting is_active to false. "
              + "Inactive products cannot be selected by users.")
  public ResponseEntity<ApiResponse<Void>> deactivatePlafond(@PathVariable Long id) {
    plafondService.deactivatePlafond(id);

    ApiResponse<Void> response =
        ApiResponse.<Void>builder()
            .success(true)
            .message("Plafond deactivated successfully. It will no longer appear for new loans.")
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(
      summary = "Activate plafond",
      description =
          "SUPER_ADMIN only - Reactivates a deactivated product. "
              + "System validates no overlap with other active product amount ranges.")
  public ResponseEntity<ApiResponse<PlafondResponse>> activatePlafond(@PathVariable Long id) {
    PlafondResponse plafond = plafondService.activatePlafond(id);

    ApiResponse<PlafondResponse> response =
        ApiResponse.<PlafondResponse>builder()
            .success(true)
            .message(
                "Plafond '"
                    + plafond.getName()
                    + "' activated successfully. "
                    + "It is now available for loan applications.")
            .data(plafond)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }
}
