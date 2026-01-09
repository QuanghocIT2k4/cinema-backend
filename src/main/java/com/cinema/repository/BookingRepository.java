package com.cinema.repository;

import com.cinema.model.entity.Booking;
import com.cinema.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm booking theo user
    List<Booking> findByUserId(Long userId);
    
    // Tìm booking theo suất chiếu
    List<Booking> findByShowtimeId(Long showtimeId);
    
    // Tìm booking theo status
    List<Booking> findByStatus(BookingStatus status);
    
    // Tìm booking theo mã đặt vé
    Optional<Booking> findByBookingCode(String bookingCode);
    
    // Tìm booking theo user và status
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
}

