package com.cinema.model.dto.response;

import com.cinema.model.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response trả về thông tin Seat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Long roomId;
    private String seatNumber; // A1, A2, B1, ...
    private String row; // A, B, C, ...
    private Integer col; // 1, 2, 3, ...
    private SeatType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



