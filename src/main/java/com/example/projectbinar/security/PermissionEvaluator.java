package com.example.projectbinar.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Permission evaluator for dynamic RBAC.
 * Used in @PreAuthorize annotations for method-level security.
 */
@Component("permissionEvaluator")
public class PermissionEvaluator {

    /**
     * Check if the authenticated user has a specific permission.
     *
     * @param authentication Current authentication object
     * @param permission Permission name to check (e.g., "LOAN_CREATE", "LOAN_REVIEW")
     * @return true if user has the permission
     */
    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(permission) || auth.equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * Check if the authenticated user has any of the specified permissions.
     *
     * @param authentication Current authentication object
     * @param permissions Permission names to check
     * @return true if user has any of the permissions
     */
    public boolean hasAnyPermission(Authentication authentication, String... permissions) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER_ADMIN has all permissions
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_SUPER_ADMIN"));
        
        if (isSuperAdmin) {
            return true;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> {
                    for (String permission : permissions) {
                        if (auth.equals(permission)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    /**
     * Check if the authenticated user has a specific role.
     *
     * @param authentication Current authentication object
     * @param role Role name (without ROLE_ prefix)
     * @return true if user has the role
     */
    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(roleWithPrefix));
    }
}
