package com.cinema.model.dto.response;

import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response trả về thông tin User (không có password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String fullName;
    private String phone;
    private String address;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}















