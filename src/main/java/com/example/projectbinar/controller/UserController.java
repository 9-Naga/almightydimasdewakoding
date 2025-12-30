package com.example.projectbinar.controller;

import com.example.projectbinar.base.ApiResponse;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();

        ApiResponse<List<User>> response = ApiResponse.<List<User>>builder()
                .message("Data user berhasil diambil")
                .success(true)
                .data(users)
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    ApiResponse<User> response = ApiResponse.<User>builder()
                            .message("Data user berhasil diambil")
                            .success(true)
                            .data(user)
                            .code(HttpStatus.OK.value())
                            .timestamp(Instant.now())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<User> response = ApiResponse.<User>builder()
                            .message("User tidak ditemukan")
                            .success(false)
                            .data(null)
                            .code(HttpStatus.NOT_FOUND.value())
                            .timestamp(Instant.now())
                            .build();
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);

        ApiResponse<User> response = ApiResponse.<User>builder()
                .message("User berhasil dibuat!")
                .success(true)
                .data(createdUser)
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);

            ApiResponse<User> response = ApiResponse.<User>builder()
                    .message("User berhasil diupdate!")
                    .success(true)
                    .data(updatedUser)
                    .code(HttpStatus.OK.value())
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = ApiResponse.<User>builder()
                    .message(e.getMessage())
                    .success(false)
                    .data(null)
                    .code(HttpStatus.NOT_FOUND.value())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.softDeleteUser(id);

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .message("User berhasil dihapus!")
                    .success(true)
                    .data(null)
                    .code(HttpStatus.OK.value())
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .message(e.getMessage())
                    .success(false)
                    .data(null)
                    .code(HttpStatus.NOT_FOUND.value())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}