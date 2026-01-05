package com.example.projectbinar.controller;

import com.example.projectbinar.entity.Role;
import com.example.projectbinar.service.RoleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController {

  @Autowired private RoleService roleService;

  @GetMapping
  public List<Role> getAllRoles() {
    return roleService.getAllRoles();
  }

  @PostMapping
  public Role createRole(@RequestBody Role role) {
    return roleService.createRole(role);
  }
}
