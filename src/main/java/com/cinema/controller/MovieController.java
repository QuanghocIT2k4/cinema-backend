package com.cinema.controller;

import com.cinema.model.dto.MovieCreateRequest;
import com.cinema.model.dto.MovieResponse;
import com.cinema.model.dto.MovieUpdateRequest;
import com.cinema.model.enums.MovieStatus;
import com.cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Lấy danh sách Movie với pagination, search và filter
     * Public: Không cần authentication
     */
    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MovieResponse> movies = movieService.getAllMovies(pageable, search, genre, status);
        return ResponseEntity.ok(movies);
    }
    
    /**
     * GET /api/movies/{id}
     * Lấy chi tiết 1 Movie
     * Public: Không cần authentication
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }
    
    /**
     * POST /api/movies
     * Tạo Movie mới (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }
    
    /**
     * PUT /api/movies/{id}
     * Cập nhật Movie (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieUpdateRequest request) {
        MovieResponse movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(movie);
    }
    
    /**
     * DELETE /api/movies/{id}
     * Xóa Movie (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}

