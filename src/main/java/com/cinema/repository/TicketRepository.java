package com.cinema.repository;

import com.cinema.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Tìm vé theo booking
    List<Ticket> findByBookingId(Long bookingId);
    
    // Tìm vé theo ghế
    List<Ticket> findBySeatId(Long seatId);
}

