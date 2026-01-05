package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.entity.Role;
import com.example.projectbinar.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "Role management endpoints for SUPER_ADMIN")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleController {

  @Autowired private RoleService roleService;

  @GetMapping
  @Operation(summary = "Get all roles", description = "SUPER_ADMIN - Get all roles")
  public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
    List<Role> roles = roleService.getAllRoles();

    ApiResponse<List<Role>> response =
        ApiResponse.<List<Role>>builder()
            .message("Roles retrieved successfully")
            .success(true)
            .data(roles)
            .code(HttpStatus.OK.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.ok(response);
  }

  @PostMapping
  @Operation(summary = "Create role", description = "SUPER_ADMIN - Create new role")
  public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
    Role createdRole = roleService.createRole(role);

    ApiResponse<Role> response =
        ApiResponse.<Role>builder()
            .message("Role created successfully")
            .success(true)
            .data(createdRole)
            .code(HttpStatus.CREATED.value())
            .timestamp(Instant.now())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
