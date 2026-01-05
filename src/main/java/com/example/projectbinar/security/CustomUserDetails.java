package com.example.projectbinar.security;

import com.example.projectbinar.entity.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

  private Long id;
  private String username;
  private String password;
  private String email;
  private String fullname;
  private Boolean isActive;
  private Collection<? extends GrantedAuthority> authorities;

  public static CustomUserDetails build(User user) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Add roles as authorities (ROLE_XXX format)
    user.getRoles()
        .forEach(
            role -> {
              authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

              // Add permissions from each role
              role.getPermissions()
                  .forEach(
                      permission -> {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                      });
            });

    return new CustomUserDetails(
        user.getId(),
        user.getUsername(),
        user.getPasswordHash(),
        user.getEmail(),
        user.getFullname(),
        user.getIsActive(),
        authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return isActive != null && isActive;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return isActive != null && isActive;
  }
}
