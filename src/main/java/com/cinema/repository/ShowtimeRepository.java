package com.cinema.repository;

import com.cinema.model.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    // Tìm suất chiếu theo phim
    List<Showtime> findByMovieId(Long movieId);
    
    // Tìm suất chiếu theo phim (dùng relationship)
    // Eager load Room và Cinema để tránh LazyInitializationException
    @Query("SELECT s FROM Showtime s " +
           "JOIN FETCH s.room r " +
           "JOIN FETCH r.cinema " +
           "JOIN FETCH s.movie " +
           "WHERE s.movie.id = :movieId")
    List<Showtime> findByMovie_Id(@Param("movieId") Long movieId);
    
    // Tìm suất chiếu theo phòng
    List<Showtime> findByRoomId(Long roomId);
    
    // Tìm suất chiếu theo phòng (dùng relationship)
    List<Showtime> findByRoom_Id(Long roomId);
    
    // Tìm suất chiếu theo ngày (range [startOfDay, endOfDay])
    // Eager load Room và Cinema để tránh LazyInitializationException
    @Query("SELECT s FROM Showtime s " +
           "JOIN FETCH s.room r " +
           "JOIN FETCH r.cinema " +
           "JOIN FETCH s.movie " +
           "WHERE s.startTime >= :startOfDay AND s.startTime <= :endOfDay")
    List<Showtime> findByStartTimeBetween(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
    
    // Kiểm tra xung đột suất chiếu trong cùng phòng
    @Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Showtime> findConflictingShowtimes(
        @Param("roomId") Long roomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}