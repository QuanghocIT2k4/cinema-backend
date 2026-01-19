package com.cinema.repository;

import com.cinema.model.entity.Ticket;
import com.cinema.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Tìm vé theo booking
    List<Ticket> findByBookingId(Long bookingId);
    
    // Tìm vé theo ghế
    List<Ticket> findBySeatId(Long seatId);

    // Tìm các vé theo danh sách ghế + showtime + status booking != CANCELLED
    List<Ticket> findBySeatIdInAndBooking_Showtime_IdAndBooking_StatusNot(
            List<Long> seatIds,
            Long showtimeId,
            BookingStatus status);

    // Tìm vé theo showtime + status booking != CANCELLED
    List<Ticket> findByBooking_Showtime_IdAndBooking_StatusNot(
            Long showtimeId,
            BookingStatus status);
}

