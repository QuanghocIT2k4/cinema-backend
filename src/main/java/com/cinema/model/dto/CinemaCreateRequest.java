package com.cinema.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request tạo Cinema mới (Admin only)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaCreateRequest {
    
    @NotBlank(message = "Tên rạp không được để trống")
    @Size(max = 200, message = "Tên rạp không được vượt quá 200 ký tự")
    private String name;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số hoặc để trống")
    private String phone;
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
}

