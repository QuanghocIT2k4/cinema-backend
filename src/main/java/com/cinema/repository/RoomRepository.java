package com.cinema.repository;

import com.cinema.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    // Tìm phòng theo rạp phim
    List<Room> findByCinemaId(Long cinemaId);
    
    // Tìm phòng theo rạp và số phòng
    Optional<Room> findByCinemaIdAndRoomNumber(Long cinemaId, String roomNumber);
    
    // Kiểm tra số phòng đã tồn tại trong rạp chưa
    boolean existsByCinemaIdAndRoomNumber(Long cinemaId, String roomNumber);
}

