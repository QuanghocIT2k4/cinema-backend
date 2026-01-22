package com.cinema.service;

import com.cinema.model.dto.response.TicketResponse;
import com.cinema.model.entity.Booking;
import com.cinema.model.entity.Ticket;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.TicketRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xử lý logic Ticket (lấy danh sách vé theo booking)
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("Không xác định được user hiện tại");
        }
        return userDetails;
    }

    public List<TicketResponse> getTicketsByBookingId(Long bookingId) {
        CustomUserDetails currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại với id: " + bookingId));

        boolean isAdmin = currentUser.getUser().getRole() == UserRole.ADMIN;
        boolean isOwner = booking.getUser().getId().equals(currentUser.getUser().getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền xem vé của booking này");
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        return tickets.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private TicketResponse convertToResponse(Ticket ticket) {
        TicketResponse res = new TicketResponse();
        res.setId(ticket.getId());
        res.setBookingId(ticket.getBooking() != null ? ticket.getBooking().getId() : null);
        if (ticket.getSeat() != null) {
            res.setSeatId(ticket.getSeat().getId());
            res.setSeatNumber(ticket.getSeat().getSeatNumber());
            res.setRow(ticket.getSeat().getRow());
            res.setCol(ticket.getSeat().getCol());
        }
        res.setPrice(ticket.getPrice());
        res.setCreatedAt(ticket.getCreatedAt());
        return res;
    }
}








