package com.cinema.model.dto;

import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request cập nhật User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
    
    private UserRole role;
    
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;
    
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số hoặc để trống")
    private String phone;
    
    private String address;
    
    private String avatar;
    
    private UserStatus status;
}

