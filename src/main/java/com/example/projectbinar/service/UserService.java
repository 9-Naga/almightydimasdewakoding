package com.example.projectbinar.service;

import com.example.projectbinar.entity.User;
import com.example.projectbinar.repository.UserRepository;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements Serializable {

  @Autowired private UserRepository userRepository;

  public User createUser(User user) {
    user.setIsActive(true);
    return userRepository.save(user);
  }

  public List<User> getAllUsers() {
    return userRepository.findByIsActiveTrue();
  }

  public Optional<User> getUserById(Long id) {
    return userRepository.findByIdAndIsActiveTrue(id);
  }

  public User updateUser(Long id, User userDetails) {
    User user =
        userRepository
            .findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan id: " + id));

    if (userDetails.getUsername() != null) {
      user.setUsername(userDetails.getUsername());
    }
    if (userDetails.getEmail() != null) {
      user.setEmail(userDetails.getEmail());
    }
    if (userDetails.getFullname() != null) {
      user.setFullname(userDetails.getFullname());
    }
    if (userDetails.getPhone() != null) {
      user.setPhone(userDetails.getPhone());
    }
    if (userDetails.getPasswordHash() != null) {
      user.setPasswordHash(userDetails.getPasswordHash());
    }
    if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
      user.setRoles(userDetails.getRoles());
    }

    return userRepository.save(user);
  }

  public void softDeleteUser(Long id) {
    User user =
        userRepository
            .findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan id: " + id));

    user.setIsActive(false);
    userRepository.save(user);
  }

  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }
}
