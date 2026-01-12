package com.cinema.controller;

import com.cinema.model.dto.UserCreateRequest;
import com.cinema.model.dto.UserResponse;
import com.cinema.model.dto.UserUpdateRequest;
import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.UserStatus;
import com.cinema.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API CRUD User
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * GET /api/users
     * Lấy danh sách User với pagination và filter (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<UserResponse> users = userService.getAllUsers(pageable, search, role, status);
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/users/{id}
     * Lấy chi tiết 1 User
     * Admin: xem được tất cả
     * Customer: chỉ xem được chính mình
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * POST /api/users
     * Tạo User mới (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * PUT /api/users/{id}
     * Cập nhật User
     * Admin: sửa được tất cả
     * Customer: chỉ sửa được chính mình
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }
    
    /**
     * DELETE /api/users/{id}
     * Xóa User (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

