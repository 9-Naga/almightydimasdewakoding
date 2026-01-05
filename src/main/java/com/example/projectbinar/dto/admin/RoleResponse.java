package com.example.projectbinar.dto.admin;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
  private Long id;
  private String name;
  private String description;
  private Set<String> permissions;
}
