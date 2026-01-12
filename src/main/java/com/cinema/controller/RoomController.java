package com.cinema.controller;

import com.cinema.model.dto.RoomCreateRequest;
import com.cinema.model.dto.RoomResponse;
import com.cinema.model.dto.RoomUpdateRequest;
import com.cinema.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Lấy danh sách Room (có thể filter theo cinemaId)
     * Public: Không cần authentication
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms(
            @RequestParam(required = false) Long cinemaId) {
        List<RoomResponse> rooms = roomService.getAllRooms(cinemaId);
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * GET /api/rooms/{id}
     * Lấy chi tiết 1 Room (bao gồm danh sách seats)
     * Public: Không cần authentication
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        RoomResponse room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }
    
    /**
     * POST /api/rooms
     * Tạo Room mới và tự động tạo ghế (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        RoomResponse room = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }
    
    /**
     * PUT /api/rooms/{id}
     * Cập nhật Room (Admin only)
     * Nếu thay đổi rows/cols, sẽ tự động xóa ghế cũ và tạo lại
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomUpdateRequest request) {
        RoomResponse room = roomService.updateRoom(id, request);
        return ResponseEntity.ok(room);
    }
    
    /**
     * DELETE /api/rooms/{id}
     * Xóa Room (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}

