package com.cinema.service;

import com.cinema.model.dto.request.UserRequest;
import com.cinema.model.dto.response.UserResponse;
import com.cinema.model.entity.User;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.UserRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic CRUD User (chỉ Admin)
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Kiểm tra user hiện tại có phải Admin không
     */
    private void checkAdminRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails.getUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Chỉ Admin mới có quyền thực hiện thao tác này");
        }
    }
    
    /**
     * Lấy tất cả users (có phân trang)
     */
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        checkAdminRole();
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Lấy user theo ID
     */
    public UserResponse getUserById(Long id) {
        checkAdminRole();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id: " + id));
        return convertToResponse(user);
    }
    
    /**
     * Tạo user mới
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        checkAdminRole();
        
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
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : com.cinema.model.enums.UserStatus.ACTIVE);
        
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }
    
    /**
     * Cập nhật user
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        checkAdminRole();
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id: " + id));
        
        // Kiểm tra username đã tồn tại (nếu thay đổi)
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        
        // Kiểm tra email đã tồn tại (nếu thay đổi)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Cập nhật thông tin
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Chỉ update password nếu có trong request
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
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
        return convertToResponse(updatedUser);
    }
    
    /**
     * Xóa user
     */
    @Transactional
    public void deleteUser(Long id) {
        checkAdminRole();
        
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User không tồn tại với id: " + id);
        }
        
        userRepository.deleteById(id);
    }
    
    /**
     * Convert User entity sang UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
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

