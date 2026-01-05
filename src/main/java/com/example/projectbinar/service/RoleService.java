package com.example.projectbinar.service;

import com.example.projectbinar.entity.Role;
import com.example.projectbinar.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  @Autowired private RoleRepository roleRepository;

  @CacheEvict(value = "roles", allEntries = true)
  public Role createRole(Role role) {
    return roleRepository.save(role);
  }

  @Cacheable(value = "roles")
  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }

  @Cacheable(value = "role", key = "#id")
  public Optional<Role> getRoleById(Long id) {
    return roleRepository.findById(id);
  }

  @CacheEvict(
      value = {"roles", "role"},
      allEntries = true)
  public void deleteRole(Long id) {
    roleRepository.deleteById(id);
  }
}
