package com.cinema.controller;

import com.cinema.model.dto.request.MovieRequest;
import com.cinema.model.dto.response.MovieActorResponse;
import com.cinema.model.dto.response.MovieResponse;
import com.cinema.model.dto.response.ReviewResponse;
import com.cinema.model.enums.MovieStatus;
import com.cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Lấy danh sách tất cả movies (có phân trang và filter)
     * Query params: page, size, genre, year, rating, sortBy, sortOrder, status
     */
    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) MovieStatus status) {
        Pageable pageable = movieService.createPageable(page, size, sortBy, sortOrder);
        Page<MovieResponse> movies = movieService.searchMovies(null, genre, year, rating, status, pageable);
        return ResponseEntity.ok(movies);
    }
    
    /**
     * GET /api/movies/search
     * Tìm kiếm movies theo name, genre, status
     * Query params: keyword, genre, status, page, size
     */
    @GetMapping("/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            // FE đang gửi param 'search', map về biến keyword
            @RequestParam(name = "search", required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = movieService.createPageable(page, size, null, null);
        Page<MovieResponse> movies = movieService.searchMovies(keyword, genre, null, null, status, pageable);
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
     * GET /api/movies/{id}/actors
     * Lấy danh sách diễn viên của một phim
     */
    @GetMapping("/{id}/actors")
    public ResponseEntity<List<MovieActorResponse>> getMovieActors(@PathVariable Long id) {
        List<MovieActorResponse> actors = movieService.getMovieActors(id);
        return ResponseEntity.ok(actors);
    }

    /**
     * GET /api/movies/{id}/reviews
     * Lấy danh sách review của một phim
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getMovieReviews(@PathVariable Long id) {
        List<ReviewResponse> reviews = movieService.getMovieReviews(id);
        return ResponseEntity.ok(reviews);
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

