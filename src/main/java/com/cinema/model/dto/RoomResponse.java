package com.cinema.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response trả về thông tin Room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private Long cinemaId;
    private String cinemaName;
    private String roomNumber;
    private Integer totalRows;
    private Integer totalCols;
    private Integer totalSeats;
    private List<SeatResponse> seats; // Danh sách ghế
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

