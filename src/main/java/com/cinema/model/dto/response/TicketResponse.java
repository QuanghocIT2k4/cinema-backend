package com.cinema.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin Ticket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private Long bookingId;
    private Long seatId;
    private String seatNumber;
    private String row;
    private Integer col;
    private BigDecimal price;
    private LocalDateTime createdAt;
}








