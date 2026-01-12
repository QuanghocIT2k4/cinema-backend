package com.cinema.controller;

import com.cinema.model.dto.AuthResponse;
import com.cinema.model.dto.LoginRequest;
import com.cinema.model.dto.RegisterRequest;
import com.cinema.model.dto.UserResponse;
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
}

