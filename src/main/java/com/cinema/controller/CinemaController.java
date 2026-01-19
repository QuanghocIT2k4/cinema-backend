package com.cinema.controller;

import com.cinema.model.dto.request.CinemaRequest;
import com.cinema.model.dto.response.CinemaResponse;
import com.cinema.service.CinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API CRUD Cinema
 */
@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {
    
    private final CinemaService cinemaService;
    
    /**
     * GET /api/cinemas
     * Lấy danh sách tất cả cinemas (có phân trang)
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<Page<CinemaResponse>> getAllCinemas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CinemaResponse> cinemas = cinemaService.getAllCinemas(pageable);
        return ResponseEntity.ok(cinemas);
    }
    
    /**
     * GET /api/cinemas/{id}
     * Lấy thông tin cinema theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CinemaResponse> getCinemaById(@PathVariable Long id) {
        CinemaResponse cinema = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(cinema);
    }
    
    /**
     * POST /api/cinemas
     * Tạo cinema mới (chỉ Admin)
     */
    @PostMapping
    public ResponseEntity<CinemaResponse> createCinema(@Valid @RequestBody CinemaRequest request) {
        CinemaResponse cinema = cinemaService.createCinema(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cinema);
    }
    
    /**
     * PUT /api/cinemas/{id}
     * Cập nhật cinema (chỉ Admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<CinemaResponse> updateCinema(
            @PathVariable Long id,
            @Valid @RequestBody CinemaRequest request) {
        CinemaResponse cinema = cinemaService.updateCinema(id, request);
        return ResponseEntity.ok(cinema);
    }
    
    /**
     * DELETE /api/cinemas/{id}
     * Xóa cinema (chỉ Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.noContent().build();
    }
}








