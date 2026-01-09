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
    List<Showtime> findByMovie_Id(Long movieId);
    
    // Tìm suất chiếu theo phòng
    List<Showtime> findByRoomId(Long roomId);
    
    // Tìm suất chiếu theo phòng (dùng relationship)
    List<Showtime> findByRoom_Id(Long roomId);
    
    // Tìm suất chiếu theo ngày
    @Query("SELECT s FROM Showtime s WHERE DATE(s.startTime) = DATE(:date)")
    List<Showtime> findByDate(@Param("date") LocalDateTime date);
    
    // Kiểm tra xung đột suất chiếu trong cùng phòng
    @Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Showtime> findConflictingShowtimes(
        @Param("roomId") Long roomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

