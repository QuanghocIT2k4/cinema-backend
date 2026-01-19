package com.cinema.service;

import com.cinema.model.dto.request.ChangePasswordRequest;
import com.cinema.model.dto.request.LoginRequest;
import com.cinema.model.dto.request.RegisterRequest;
import com.cinema.model.dto.response.AuthResponse;
import com.cinema.model.dto.response.UserResponse;
import com.cinema.model.entity.User;
import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.UserStatus;
import com.cinema.repository.UserRepository;
import com.cinema.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic authentication: đăng ký, đăng nhập, lấy thông tin user
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Đăng ký tài khoản Customer mới
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Tạo User mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CUSTOMER); // Mặc định là CUSTOMER
        user.setStatus(UserStatus.ACTIVE); // Mặc định là ACTIVE
        
        // Lưu vào database
        User savedUser = userRepository.save(user);
        
        // Convert sang UserResponse (không có password)
        return convertToUserResponse(savedUser);
    }
    
    /**
     * Đăng nhập và trả về JWT token
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate với Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // Lấy UserDetails từ authentication
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        
        // Tìm User trong database
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Kiểm tra status
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        
        // Tạo JWT token
        String token = jwtUtils.generateToken(user.getUsername());
        
        // Convert sang UserResponse
        UserResponse userResponse = convertToUserResponse(user);
        
        // Trả về token + user info
        return new AuthResponse(token, userResponse);
    }
    
    /**
     * Lấy thông tin user hiện tại (từ JWT token đã authenticate)
     */
    public UserResponse getCurrentUser() {
        // Lấy username từ SecurityContext (đã được set bởi JwtAuthenticationFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        
        // Lấy username từ UserDetails nếu có (từ CustomUserDetails), nếu không thì lấy từ authentication.getName()
        final String username;
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else {
            username = authentication.getName();
        }
        
        // Tìm User trong database
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User không tồn tại với username: " + username));
        
        // Convert sang UserResponse
        return convertToUserResponse(user);
    }

    /**
     * Đổi mật khẩu cho user hiện tại
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        final String username;
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = authentication.getName();
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với username: " + username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    /**
     * Convert User entity sang UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}

