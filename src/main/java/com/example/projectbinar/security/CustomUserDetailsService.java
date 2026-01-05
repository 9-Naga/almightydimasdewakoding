package com.example.projectbinar.security;

import com.example.projectbinar.entity.User;
import com.example.projectbinar.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    if (!user.getIsActive()) {
      throw new UsernameNotFoundException("User account is deactivated: " + username);
    }

    return CustomUserDetails.build(user);
  }

  @Transactional
  public UserDetails loadUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

    return CustomUserDetails.build(user);
  }
}
