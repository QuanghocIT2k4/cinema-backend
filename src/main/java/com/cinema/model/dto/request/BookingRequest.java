package com.cinema.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dùng cho API tạo Booking (đặt vé)
 *
 * Gồm:
 * - showtimeId: suất chiếu cần đặt
 * - seatIds: danh sách ghế được chọn
 * - refreshments: danh sách đồ ăn/uống kèm số lượng (tùy chọn)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "ShowtimeId không được để trống")
    private Long showtimeId;

    @NotEmpty(message = "Danh sách ghế không được để trống")
    private List<@NotNull(message = "SeatId không được để trống") Long> seatIds;

    /**
     * Danh sách đồ ăn/uống đặt kèm (có thể null hoặc rỗng)
     */
    @Valid
    private List<RefreshmentOrder> refreshments;

    /**
     * Thông tin 1 loại đồ ăn/uống trong Booking
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshmentOrder {

        @NotNull(message = "RefreshmentId không được để trống")
        private Long refreshmentId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải >= 1")
        private Integer quantity;
    }
}








