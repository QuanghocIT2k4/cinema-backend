package com.cinema.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho việc cập nhật thông tin profile của chính user đang đăng nhập.
 * Email/username không cho chỉnh ở đây để tránh xung đột định danh.
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 255, message = "Avatar URL không được vượt quá 255 ký tự")
    private String avatar;
}





