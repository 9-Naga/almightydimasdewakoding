package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.plafond.PlafondRequest;
import com.example.projectbinar.dto.plafond.PlafondResponse;
import com.example.projectbinar.service.PlafondService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/plafonds")
@Tag(name = "Plafond/Products", description = "Loan product management (public GET, admin for others)")
public class PlafondController {

    private final PlafondService plafondService;

    public PlafondController(PlafondService plafondService) {
        this.plafondService = plafondService;
    }

    @GetMapping
    @Operation(summary = "Get all active plafonds", description = "Public endpoint - no authentication required")
    public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllActivePlafonds() {
        List<PlafondResponse> plafonds = plafondService.getAllActivePlafonds();
        
        ApiResponse<List<PlafondResponse>> response = ApiResponse.<List<PlafondResponse>>builder()
                .success(true)
                .message("Plafonds retrieved successfully")
                .data(plafonds)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get plafond by ID", description = "Public endpoint - no authentication required")
    public ResponseEntity<ApiResponse<PlafondResponse>> getPlafondById(@PathVariable Long id) {
        PlafondResponse plafond = plafondService.getPlafondById(id);
        
        ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
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
    @Operation(summary = "Get all plafonds including inactive", description = "Admin only - includes inactive plafonds")
    public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllPlafonds() {
        List<PlafondResponse> plafonds = plafondService.getAllPlafonds();
        
        ApiResponse<List<PlafondResponse>> response = ApiResponse.<List<PlafondResponse>>builder()
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
    @Operation(summary = "Create new plafond", description = "SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<PlafondResponse>> createPlafond(@Valid @RequestBody PlafondRequest request) {
        PlafondResponse plafond = plafondService.createPlafond(request);
        
        ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                .success(true)
                .message("Plafond created successfully")
                .data(plafond)
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update plafond", description = "SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<PlafondResponse>> updatePlafond(@PathVariable Long id, @Valid @RequestBody PlafondRequest request) {
        PlafondResponse plafond = plafondService.updatePlafond(id, request);
        
        ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                .success(true)
                .message("Plafond updated successfully")
                .data(plafond)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Deactivate plafond (soft delete)", description = "SUPER_ADMIN only - sets is_active to false")
    public ResponseEntity<ApiResponse<Void>> deactivatePlafond(@PathVariable Long id) {
        plafondService.deactivatePlafond(id);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Plafond deactivated successfully")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
