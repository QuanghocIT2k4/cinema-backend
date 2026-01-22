package com.cinema.controller;

import com.cinema.model.dto.request.RoomRequest;
import com.cinema.model.dto.response.RoomResponse;
import com.cinema.model.dto.response.SeatResponse;
import com.cinema.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API CRUD Room
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    
    /**
     * GET /api/rooms
     * Lấy danh sách tất cả rooms (có phân trang)
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomResponse> rooms = roomService.getAllRooms(pageable);
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * GET /api/rooms/cinema/{cinemaId}
     * Lấy danh sách rooms theo cinema ID
     */
    @GetMapping("/cinema/{cinemaId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByCinemaId(@PathVariable Long cinemaId) {
        List<RoomResponse> rooms = roomService.getRoomsByCinemaId(cinemaId);
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * GET /api/rooms/{id}
     * Lấy thông tin room theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        RoomResponse room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    /**
     * GET /api/rooms/{id}/seats
     * Lấy danh sách ghế theo room ID (public, không cần admin)
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> getSeatsByRoomId(@PathVariable Long id) {
        List<SeatResponse> seats = roomService.getSeatsByRoomId(id);
        return ResponseEntity.ok(seats);
    }
    
    /**
     * POST /api/rooms
     * Tạo room mới và tự động tạo ghế (chỉ Admin)
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse room = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }
    
    /**
     * PUT /api/rooms/{id}
     * Cập nhật room (chỉ Admin)
     * Lưu ý: Nếu thay đổi rows/cols, sẽ xóa ghế cũ và tạo lại
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        RoomResponse room = roomService.updateRoom(id, request);
        return ResponseEntity.ok(room);
    }
    
    /**
     * DELETE /api/rooms/{id}
     * Xóa room (chỉ Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}













