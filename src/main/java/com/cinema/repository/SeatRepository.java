package com.cinema.repository;

import com.cinema.model.entity.Seat;
import com.cinema.model.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    // Tìm ghế theo phòng
    List<Seat> findByRoomId(Long roomId);
    
    // Tìm ghế theo loại
    List<Seat> findByType(SeatType type);
    
    // Tìm ghế theo phòng và loại
    List<Seat> findByRoomIdAndType(Long roomId, SeatType type);
}

