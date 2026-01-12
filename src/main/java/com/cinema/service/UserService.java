package com.cinema.service;

import com.cinema.model.dto.UserCreateRequest;
import com.cinema.model.dto.UserResponse;
import com.cinema.model.dto.UserUpdateRequest;
import com.cinema.model.entity.User;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic CRUD User
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Lấy danh sách User với pagination và filter (Admin only)
     */
    public Page<UserResponse> getAllUsers(Pageable pageable, String search, UserRole role, com.cinema.model.enums.UserStatus status) {
        Specification<User> spec = Specification.where(null);
        
        // Search theo username, email, fullName
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("username")), "%" + keyword + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + keyword + "%"),
                    cb.like(cb.lower(root.get("fullName")), "%" + keyword + "%")
                )
            );
        }
        
        // Filter theo role
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        
        // Filter theo status
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::convertToUserResponse);
    }
    
    /**
     * Lấy chi tiết 1 User
     * Admin: xem được tất cả
     * Customer: chỉ xem được chính mình
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Kiểm tra quyền truy cập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Nếu không phải Admin và không phải chính mình → Forbidden
        if (currentUser.getRole() != UserRole.ADMIN && !user.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Không có quyền truy cập");
        }
        
        return convertToUserResponse(user);
    }
    
    /**
     * Tạo User mới (Admin only)
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : com.cinema.model.enums.UserStatus.ACTIVE);
        
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }
    
    /**
     * Cập nhật User
     * Admin: sửa được tất cả
     * Customer: chỉ sửa được chính mình (không được sửa role, status)
     */
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Kiểm tra quyền truy cập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Nếu không phải Admin và không phải chính mình → Forbidden
        if (currentUser.getRole() != UserRole.ADMIN && !user.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Không có quyền truy cập");
        }
        
        // Customer không được sửa role và status
        if (currentUser.getRole() != UserRole.ADMIN) {
            if (request.getRole() != null || request.getStatus() != null) {
                throw new RuntimeException("Không có quyền thay đổi role hoặc status");
            }
        }
        
        // Kiểm tra email đã tồn tại (nếu thay đổi email)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }
        
        // Cập nhật các field khác
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Xóa User (Admin only)
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User không tồn tại");
        }
        userRepository.deleteById(id);
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

