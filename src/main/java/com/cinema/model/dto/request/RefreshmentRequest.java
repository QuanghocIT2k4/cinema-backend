package com.cinema.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO cho tạo/cập nhật Refreshment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshmentRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String picture;

    @NotNull
    @Min(value = 0, message = "Giá phải >= 0")
    private BigDecimal price;

    private Boolean isCurrent = true;
}


