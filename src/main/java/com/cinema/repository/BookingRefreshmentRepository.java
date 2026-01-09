package com.cinema.repository;

import com.cinema.model.entity.BookingRefreshment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRefreshmentRepository extends JpaRepository<BookingRefreshment, Long> {
    // Tìm đồ ăn/đồ uống theo booking
    List<BookingRefreshment> findByBookingId(Long bookingId);
    
    // Tìm đồ ăn/đồ uống theo refreshment
    List<BookingRefreshment> findByRefreshmentId(Long refreshmentId);
}

