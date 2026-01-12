package com.cinema.controller;

import com.cinema.model.dto.CinemaCreateRequest;
import com.cinema.model.dto.CinemaResponse;
import com.cinema.model.dto.CinemaUpdateRequest;
import com.cinema.service.CinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Lấy danh sách tất cả Cinema
     * Public: Không cần authentication
     */
    @GetMapping
    public ResponseEntity<List<CinemaResponse>> getAllCinemas() {
        List<CinemaResponse> cinemas = cinemaService.getAllCinemas();
        return ResponseEntity.ok(cinemas);
    }
    
    /**
     * GET /api/cinemas/{id}
     * Lấy chi tiết 1 Cinema (bao gồm danh sách rooms)
     * Public: Không cần authentication
     */
    @GetMapping("/{id}")
    public ResponseEntity<CinemaResponse> getCinemaById(@PathVariable Long id) {
        CinemaResponse cinema = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(cinema);
    }
    
    /**
     * POST /api/cinemas
     * Tạo Cinema mới (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaResponse> createCinema(@Valid @RequestBody CinemaCreateRequest request) {
        CinemaResponse cinema = cinemaService.createCinema(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cinema);
    }
    
    /**
     * PUT /api/cinemas/{id}
     * Cập nhật Cinema (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaResponse> updateCinema(
            @PathVariable Long id,
            @Valid @RequestBody CinemaUpdateRequest request) {
        CinemaResponse cinema = cinemaService.updateCinema(id, request);
        return ResponseEntity.ok(cinema);
    }
    
    /**
     * DELETE /api/cinemas/{id}
     * Xóa Cinema (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.noContent().build();
    }
}

