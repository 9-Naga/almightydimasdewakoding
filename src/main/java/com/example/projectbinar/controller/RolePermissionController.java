package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.dto.admin.*;
import com.example.projectbinar.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin - Role & Permission", description = "Role and permission management for SUPER_ADMIN")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    // Role endpoints
    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "SUPER_ADMIN - Get all roles with their permissions")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = rolePermissionService.getAllRoles();
        
        ApiResponse<List<RoleResponse>> response = ApiResponse.<List<RoleResponse>>builder()
                .success(true)
                .message("Roles retrieved successfully")
                .data(roles)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "Get role by ID", description = "SUPER_ADMIN - Get role details with permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = rolePermissionService.getRoleById(id);
        
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role retrieved successfully")
                .data(role)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/roles")
    @Operation(summary = "Create role", description = "SUPER_ADMIN - Create new role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = rolePermissionService.createRole(request);
        
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role created successfully")
                .data(role)
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "Update role", description = "SUPER_ADMIN - Update role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        RoleResponse role = rolePermissionService.updateRole(id, request);
        
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role updated successfully")
                .data(role)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    // Permission endpoints
    @GetMapping("/permissions")
    @Operation(summary = "Get all permissions", description = "SUPER_ADMIN - Get all available permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = rolePermissionService.getAllPermissions();
        
        ApiResponse<List<PermissionResponse>> response = ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permissions retrieved successfully")
                .data(permissions)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/permissions/{id}")
    @Operation(summary = "Get permission by ID", description = "SUPER_ADMIN - Get permission details")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse permission = rolePermissionService.getPermissionById(id);
        
        ApiResponse<PermissionResponse> response = ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permission retrieved successfully")
                .data(permission)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    // Role-Permission mapping
    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Assign permission to role", description = "SUPER_ADMIN - Assign permission to role")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermission(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionRequest request) {
        RoleResponse role = rolePermissionService.assignPermissionToRole(roleId, request.getPermissionId());
        
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Permission assigned to role successfully")
                .data(role)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from role", description = "SUPER_ADMIN - Remove permission from role")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        RoleResponse role = rolePermissionService.removePermissionFromRole(roleId, permissionId);
        
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Permission removed from role successfully")
                .data(role)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
