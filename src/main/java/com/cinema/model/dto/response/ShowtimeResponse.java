package com.cinema.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho response trả về thông tin Showtime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {
    private Long id;

    private Long movieId;
    private String movieTitle;

    private Long roomId;
    private String roomNumber;

    private Long cinemaId;
    private String cinemaName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal price;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}






