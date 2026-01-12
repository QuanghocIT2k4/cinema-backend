package com.cinema.model.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request cập nhật Room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {
    
    @Size(max = 10, message = "Số phòng không được vượt quá 10 ký tự")
    private String roomNumber;
    
    @Positive(message = "Số hàng ghế phải lớn hơn 0")
    private Integer totalRows;
    
    @Positive(message = "Số ghế mỗi hàng phải lớn hơn 0")
    private Integer totalCols;
}

