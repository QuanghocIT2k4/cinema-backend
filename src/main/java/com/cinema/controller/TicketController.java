package com.cinema.controller;

import com.cinema.model.dto.response.TicketResponse;
import com.cinema.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý Ticket APIs
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * GET /api/tickets/booking/{bookingId}
     * Lấy danh sách vé theo booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByBooking(
            @PathVariable Long bookingId) {
        List<TicketResponse> tickets = ticketService.getTicketsByBookingId(bookingId);
        return ResponseEntity.ok(tickets);
    }
}








