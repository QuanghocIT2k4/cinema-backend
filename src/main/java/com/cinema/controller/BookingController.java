package com.cinema.controller;

import com.cinema.model.dto.request.BookingRequest;
import com.cinema.model.dto.response.BookingResponse;
import com.cinema.model.enums.BookingStatus;
import com.cinema.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API Booking:
 * - Đặt vé
 * - Danh sách booking (Admin xem tất cả, Customer xem của mình)
 * - Chi tiết booking
 * - Xác nhận thanh toán
 * - Hủy booking
 * - Danh sách ghế đã đặt theo showtime
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/bookings
     * Tạo booking (đặt vé)
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookings
     * Admin: xem tất cả booking
     * Customer: chỉ xem booking của chính mình
     */
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookingResponse> bookings = bookingService.getBookings(status, page, size);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/bookings/{id}
     * Lấy chi tiết booking
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/bookings/{id}/confirm
     * Xác nhận thanh toán (Admin only)
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/bookings/{id}/cancel
     * Hủy booking (Customer: chỉ hủy của mình, Admin: hủy bất kỳ)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookings/showtime/{showtimeId}/seats
     * Lấy danh sách ghế đã đặt cho 1 showtime
     */
    @GetMapping("/showtime/{showtimeId}/seats")
    public ResponseEntity<List<BookingResponse.TicketSummary>> getBookedSeatsForShowtime(
            @PathVariable Long showtimeId) {
        List<BookingResponse.TicketSummary> seats = bookingService.getBookedSeatsForShowtime(showtimeId);
        return ResponseEntity.ok(seats);
    }
}









