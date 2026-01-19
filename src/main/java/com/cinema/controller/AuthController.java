package com.cinema.controller;

import com.cinema.model.dto.request.ChangePasswordRequest;
import com.cinema.model.dto.request.LoginRequest;
import com.cinema.model.dto.request.RegisterRequest;
import com.cinema.model.dto.response.AuthResponse;
import com.cinema.model.dto.response.UserResponse;
import com.cinema.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API authentication: đăng ký, đăng nhập, lấy thông tin user
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * POST /api/auth/register
     * Đăng ký tài khoản Customer mới
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/auth/login
     * Đăng nhập và nhận JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/auth/me
     * Lấy thông tin user hiện tại (từ JWT token)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/auth/change-password
     * Đổi mật khẩu cho user hiện tại
     */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}

