package com.example.projectbinar.service;

import com.example.projectbinar.dto.admin.PermissionResponse;
import com.example.projectbinar.dto.admin.RoleRequest;
import com.example.projectbinar.dto.admin.RoleResponse;
import com.example.projectbinar.entity.Permission;
import com.example.projectbinar.entity.Role;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.PermissionRepository;
import com.example.projectbinar.repository.RoleRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolePermissionService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  public RolePermissionService(
      RoleRepository roleRepository, PermissionRepository permissionRepository) {
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
  }

  // Role methods
  public List<RoleResponse> getAllRoles() {
    return roleRepository.findAll().stream()
        .map(this::mapRoleToResponse)
        .collect(Collectors.toList());
  }

  public RoleResponse getRoleById(Long id) {
    Role role =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    return mapRoleToResponse(role);
  }

  @Transactional
  public RoleResponse createRole(RoleRequest request) {
    if (roleRepository.findByName(request.getName()).isPresent()) {
      throw new BadRequestException("Role with name '" + request.getName() + "' already exists");
    }

    Role role =
        Role.builder()
            .name(request.getName().toUpperCase())
            .description(request.getDescription())
            .build();

    Role savedRole = roleRepository.save(role);
    return mapRoleToResponse(savedRole);
  }

  @Transactional
  public RoleResponse updateRole(Long id, RoleRequest request) {
    Role role =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

    if (!role.getName().equals(request.getName())
        && roleRepository.findByName(request.getName()).isPresent()) {
      throw new BadRequestException("Role with name '" + request.getName() + "' already exists");
    }

    role.setName(request.getName().toUpperCase());
    role.setDescription(request.getDescription());

    Role savedRole = roleRepository.save(role);
    return mapRoleToResponse(savedRole);
  }

  // Permission methods
  public List<PermissionResponse> getAllPermissions() {
    return permissionRepository.findAll().stream()
        .map(this::mapPermissionToResponse)
        .collect(Collectors.toList());
  }

  public PermissionResponse getPermissionById(Long id) {
    Permission permission =
        permissionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    return mapPermissionToResponse(permission);
  }

  // Role-Permission management
  @Transactional
  public RoleResponse assignPermissionToRole(Long roleId, Long permissionId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    Permission permission =
        permissionRepository
            .findById(permissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

    role.getPermissions().add(permission);
    Role savedRole = roleRepository.save(role);
    return mapRoleToResponse(savedRole);
  }

  @Transactional
  public RoleResponse removePermissionFromRole(Long roleId, Long permissionId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    Permission permission =
        permissionRepository
            .findById(permissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

    role.getPermissions().remove(permission);
    Role savedRole = roleRepository.save(role);
    return mapRoleToResponse(savedRole);
  }

  public Set<String> getPermissionsByRoleId(Long roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

    return role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
  }

  private RoleResponse mapRoleToResponse(Role role) {
    Set<String> permissionNames =
        role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());

    return RoleResponse.builder()
        .id(role.getId())
        .name(role.getName())
        .description(role.getDescription())
        .permissions(permissionNames)
        .build();
  }

  private PermissionResponse mapPermissionToResponse(Permission permission) {
    return PermissionResponse.builder()
        .id(permission.getId())
        .name(permission.getName())
        .description(permission.getDescription())
        .build();
  }
}
