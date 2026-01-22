package com.cinema.repository;

import com.cinema.model.entity.Booking;
import com.cinema.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Tìm booking theo user
    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    List<Booking> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    
    // Tìm booking theo suất chiếu
    List<Booking> findByShowtimeId(Long showtimeId);
    
    // Tìm booking theo status
    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    List<Booking> findByStatus(BookingStatus status);

    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    
    // Tìm booking theo mã đặt vé
    Optional<Booking> findByBookingCode(String bookingCode);
    
    // Tìm booking theo user và status
    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    // Override findAll để fetch user
    @Override
    @EntityGraph(attributePaths = {"user", "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema"})
    Page<Booking> findAll(Pageable pageable);
}

