package com.cinema.model.dto.response;

import com.cinema.model.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về cho Booking:
 * - Thông tin booking + tổng tiền
 * - Tóm tắt Showtime (movie, cinema, room)
 * - Danh sách vé (tickets)
 * - Danh sách đồ ăn/uống (refreshments)
 */
@Data
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private Long userId;
    private Long showtimeId;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime paymentTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummary user;
    private ShowtimeSummary showtime;
    private List<TicketSummary> tickets;
    private List<BookingRefreshmentSummary> refreshments;

    @Data
    public static class ShowtimeSummary {
        private Long id;
        private Long movieId;
        private String movieTitle;
        private Long cinemaId;
        private String cinemaName;
        private Long roomId;
        private String roomNumber;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal price;
    }

    @Data
    public static class TicketSummary {
        private Long id;
        private Long seatId;
        private String seatNumber;
        /**
         * Hàng ghế: dạng chữ (A, B, C, ...)
         */
        private String row;
        /**
         * Số ghế trong hàng (1, 2, 3, ...)
         */
        private Integer col;
        private BigDecimal price;
    }

    @Data
    public static class BookingRefreshmentSummary {
        private Long id;
        private Long refreshmentId;
        private String name;
        private String picture;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
    }

    @Data
    public static class UserSummary {
        private Long id;
        private String email;
        private String fullName;
    }
}


