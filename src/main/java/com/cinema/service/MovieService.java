package com.cinema.service;

import com.cinema.model.dto.MovieCreateRequest;
import com.cinema.model.dto.MovieResponse;
import com.cinema.model.dto.MovieUpdateRequest;
import com.cinema.model.entity.Movie;
import com.cinema.model.enums.MovieStatus;
import com.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service xử lý logic CRUD Movie
 */
@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    /**
     * Lấy danh sách Movie với pagination, search và filter
     */
    public Page<MovieResponse> getAllMovies(Pageable pageable, String search, String genre, MovieStatus status) {
        Specification<Movie> spec = Specification.where(null);
        
        // Search theo title
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("title")), "%" + keyword + "%")
            );
        }
        
        // Filter theo genre
        if (genre != null && !genre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("genre"), genre.trim())
            );
        }
        
        // Filter theo status
        if (status != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("status"), status)
            );
        }
        
        Page<Movie> movies = movieRepository.findAll(spec, pageable);
        return movies.map(this::convertToMovieResponse);
    }
    
    /**
     * Lấy chi tiết 1 Movie
     */
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie không tồn tại"));
        return convertToMovieResponse(movie);
    }
    
    /**
     * Tạo Movie mới (Admin only)
     */
    @Transactional
    public MovieResponse createMovie(MovieCreateRequest request) {
        // Validate: releaseDate < endDate
        if (request.getReleaseDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày khởi chiếu phải trước ngày kết thúc");
        }
        
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setPoster(request.getPoster());
        movie.setTrailer(request.getTrailer());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setEndDate(request.getEndDate());
        movie.setStatus(request.getStatus() != null ? request.getStatus() : MovieStatus.COMING_SOON);
        movie.setAgeRating(request.getAgeRating());
        
        Movie savedMovie = movieRepository.save(movie);
        return convertToMovieResponse(savedMovie);
    }
    
    /**
     * Cập nhật Movie (Admin only)
     */
    @Transactional
    public MovieResponse updateMovie(Long id, MovieUpdateRequest request) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie không tồn tại"));
        
        // Validate: releaseDate < endDate (nếu có thay đổi)
        LocalDate releaseDate = request.getReleaseDate() != null ? request.getReleaseDate() : movie.getReleaseDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : movie.getEndDate();
        if (releaseDate.isAfter(endDate)) {
            throw new RuntimeException("Ngày khởi chiếu phải trước ngày kết thúc");
        }
        
        // Cập nhật các field
        if (request.getTitle() != null) {
            movie.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            movie.setDescription(request.getDescription());
        }
        if (request.getGenre() != null) {
            movie.setGenre(request.getGenre());
        }
        if (request.getDuration() != null) {
            movie.setDuration(request.getDuration());
        }
        if (request.getPoster() != null) {
            movie.setPoster(request.getPoster());
        }
        if (request.getTrailer() != null) {
            movie.setTrailer(request.getTrailer());
        }
        if (request.getReleaseDate() != null) {
            movie.setReleaseDate(request.getReleaseDate());
        }
        if (request.getEndDate() != null) {
            movie.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        }
        if (request.getAgeRating() != null) {
            movie.setAgeRating(request.getAgeRating());
        }
        
        Movie updatedMovie = movieRepository.save(movie);
        return convertToMovieResponse(updatedMovie);
    }
    
    /**
     * Xóa Movie (Admin only)
     */
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie không tồn tại");
        }
        movieRepository.deleteById(id);
    }
    
    /**
     * Convert Movie entity sang MovieResponse DTO
     */
    private MovieResponse convertToMovieResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setGenre(movie.getGenre());
        response.setDuration(movie.getDuration());
        response.setPoster(movie.getPoster());
        response.setTrailer(movie.getTrailer());
        response.setReleaseDate(movie.getReleaseDate());
        response.setEndDate(movie.getEndDate());
        response.setStatus(movie.getStatus());
        response.setAgeRating(movie.getAgeRating());
        response.setCreatedAt(movie.getCreatedAt());
        response.setUpdatedAt(movie.getUpdatedAt());
        return response;
    }
}

