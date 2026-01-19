package com.cinema.model.dto.request;

import com.cinema.model.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request tạo/cập nhật Room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    
    @NotNull(message = "Cinema ID không được để trống")
    private Long cinemaId;
    
    @NotBlank(message = "Số phòng không được để trống")
    @Size(max = 10, message = "Số phòng không được vượt quá 10 ký tự")
    private String roomNumber;
    
    @NotNull(message = "Số hàng ghế không được để trống")
    @Positive(message = "Số hàng ghế phải lớn hơn 0")
    private Integer totalRows; // Số hàng ghế
    
    @NotNull(message = "Số ghế mỗi hàng không được để trống")
    @Positive(message = "Số ghế mỗi hàng phải lớn hơn 0")
    private Integer totalCols; // Số ghế mỗi hàng
    
    // Optional: Loại ghế mặc định (NORMAL hoặc VIP)
    private SeatType defaultSeatType = SeatType.NORMAL;
}








