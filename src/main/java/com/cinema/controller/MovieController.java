package com.cinema.controller;

import com.cinema.model.dto.request.MovieRequest;
import com.cinema.model.dto.response.MovieResponse;
import com.cinema.model.enums.MovieStatus;
import com.cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API CRUD Movie
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    
    private final MovieService movieService;
    
    /**
     * GET /api/movies
     * Lấy danh sách tất cả movies (có phân trang)
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieResponse> movies = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(movies);
    }
    
    /**
     * GET /api/movies/search
     * Tìm kiếm movies theo name, genre, status
     * Query params: keyword, genre, status, page, size
     */
    @GetMapping("/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieResponse> movies = movieService.searchMovies(keyword, genre, status, pageable);
        return ResponseEntity.ok(movies);
    }
    
    /**
     * GET /api/movies/{id}
     * Lấy thông tin movie theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }
    
    /**
     * POST /api/movies
     * Tạo movie mới (chỉ Admin)
     */
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }
    
    /**
     * PUT /api/movies/{id}
     * Cập nhật movie (chỉ Admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {
        MovieResponse movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(movie);
    }
    
    /**
     * DELETE /api/movies/{id}
     * Xóa movie (chỉ Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}

