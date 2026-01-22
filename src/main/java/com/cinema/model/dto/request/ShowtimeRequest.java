package com.cinema.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho request tạo/cập nhật Showtime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeRequest {

    @NotNull(message = "Movie ID không được để trống")
    private Long movieId;

    @NotNull(message = "Room ID không được để trống")
    private Long roomId;

    @NotNull(message = "Start time không được để trống")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Giá vé không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá vé phải lớn hơn 0")
    private BigDecimal price;
}











