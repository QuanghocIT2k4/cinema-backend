package com.cinema.controller;

import com.cinema.model.dto.request.ShowtimeRequest;
import com.cinema.model.dto.response.ShowtimeResponse;
import com.cinema.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller xử lý các API CRUD Showtime
 */
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    /**
     * GET /api/showtimes
     * Lấy danh sách tất cả showtimes (có phân trang)
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<Page<ShowtimeResponse>> getAllShowtimes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShowtimeResponse> showtimes = showtimeService.getAllShowtimes(pageable);
        return ResponseEntity.ok(showtimes);
    }

    /**
     * GET /api/showtimes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    /**
     * GET /api/showtimes/movie/{movieId}
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovieId(movieId));
    }

    /**
     * GET /api/showtimes/date/{date}
     * date format: yyyy-MM-dd
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByDate(@PathVariable String date) {
        LocalDate parsed = LocalDate.parse(date);
        return ResponseEntity.ok(showtimeService.getShowtimesByDate(parsed));
    }

    /**
     * POST /api/showtimes
     */
    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse created = showtimeService.createShowtime(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/showtimes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> updateShowtime(
            @PathVariable Long id,
            @Valid @RequestBody ShowtimeRequest request) {
        return ResponseEntity.ok(showtimeService.updateShowtime(id, request));
    }

    /**
     * DELETE /api/showtimes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }
}













