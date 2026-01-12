package com.cinema.model.dto;

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
    private String seatNumber;
    private String row;
    private Integer col;
    private SeatType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

