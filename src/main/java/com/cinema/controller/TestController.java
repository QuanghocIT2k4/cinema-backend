package com.cinema.controller;

import com.cinema.model.entity.User;
import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.UserStatus;
import com.cinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test Controller - Dùng để test kết nối MySQL và CRUD cơ bản
 * Controller này sẽ được xóa sau khi test xong
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Test kết nối database
     * GET /api/test/db
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = userRepository.count();
            response.put("status", "success");
            response.put("message", "Database connection successful");
            response.put("userCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test CREATE - Tạo user mới
     * POST /api/test/users
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = new User();
            user.setUsername(request.get("username"));
            user.setEmail(request.get("email"));
            user.setPassword(request.get("password")); // Trong thực tế phải hash password
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(UserStatus.ACTIVE);
            user.setFullName(request.get("fullName"));

            User savedUser = userRepository.save(user);
            
            response.put("status", "success");
            response.put("message", "User created successfully");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test READ - Lấy tất cả users
     * GET /api/test/users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userRepository.findAll();
            response.put("status", "success");
            response.put("message", "Users retrieved successfully");
            response.put("count", users.size());
            response.put("users", users.stream()
                .map(user -> Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString(),
                    "status", user.getStatus().toString()
                ))
                .toList()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve users: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test READ - Lấy user theo ID
     * GET /api/test/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("status", "success");
                response.put("message", "User found");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString(),
                    "status", user.getStatus().toString(),
                    "fullName", user.getFullName() != null ? user.getFullName() : ""
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test UPDATE - Cập nhật user
     * PUT /api/test/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (request.containsKey("fullName")) {
                    user.setFullName(request.get("fullName"));
                }
                if (request.containsKey("phone")) {
                    user.setPhone(request.get("phone"));
                }
                
                User updatedUser = userRepository.save(user);
                response.put("status", "success");
                response.put("message", "User updated successfully");
                response.put("user", Map.of(
                    "id", updatedUser.getId(),
                    "username", updatedUser.getUsername(),
                    "email", updatedUser.getEmail(),
                    "fullName", updatedUser.getFullName() != null ? updatedUser.getFullName() : ""
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test DELETE - Xóa user
     * DELETE /api/test/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                response.put("status", "success");
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test Repository methods - Tìm user theo username
     * GET /api/test/users/username/{username}
     */
    @GetMapping("/users/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("status", "success");
                response.put("message", "User found");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "User not found with username: " + username);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

