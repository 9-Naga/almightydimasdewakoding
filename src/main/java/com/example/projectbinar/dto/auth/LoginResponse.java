package com.example.projectbinar.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String username;
    private String email;
    private String fullname;
    private Set<String> roles;
    private Set<String> permissions;
}
