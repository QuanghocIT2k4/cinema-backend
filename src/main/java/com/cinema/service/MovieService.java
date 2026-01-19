package com.cinema.service;

import com.cinema.model.dto.request.MovieRequest;
import com.cinema.model.dto.response.MovieResponse;
import com.cinema.model.entity.Movie;
import com.cinema.model.enums.MovieStatus;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.MovieRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic CRUD Movie
 */
@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    /**
     * Kiểm tra user hiện tại có phải Admin không
     */
    private void checkAdminRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails.getUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Chỉ Admin mới có quyền thực hiện thao tác này");
        }
    }
    
    /**
     * Lấy tất cả movies (có phân trang)
     */
    public Page<MovieResponse> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Lấy movie theo ID
     */
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie không tồn tại với id: " + id));
        return convertToResponse(movie);
    }
    
    /**
     * Tìm kiếm movies theo name, genre, status
     */
    public Page<MovieResponse> searchMovies(String keyword, String genre, MovieStatus status, Pageable pageable) {
        // Nếu có keyword, tìm theo title
        if (keyword != null && !keyword.trim().isEmpty()) {
            return movieRepository.findByTitleContaining(keyword.trim())
                    .stream()
                    .filter(movie -> {
                        if (genre != null && !genre.trim().isEmpty() && !movie.getGenre().equalsIgnoreCase(genre.trim())) {
                            return false;
                        }
                        if (status != null && movie.getStatus() != status) {
                            return false;
                        }
                        return true;
                    })
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList())
                    .stream()
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                int start = (int) pageable.getOffset();
                                int end = Math.min((start + pageable.getPageSize()), list.size());
                                return new org.springframework.data.domain.PageImpl<>(
                                        list.subList(start, end),
                                        pageable,
                                        list.size()
                                );
                            }
                    ));
        }
        
        // Nếu có genre, tìm theo genre
        if (genre != null && !genre.trim().isEmpty()) {
            return movieRepository.findByGenre(genre.trim())
                    .stream()
                    .filter(movie -> {
                        if (status != null && movie.getStatus() != status) {
                            return false;
                        }
                        return true;
                    })
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList())
                    .stream()
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                int start = (int) pageable.getOffset();
                                int end = Math.min((start + pageable.getPageSize()), list.size());
                                return new org.springframework.data.domain.PageImpl<>(
                                        list.subList(start, end),
                                        pageable,
                                        list.size()
                                );
                            }
                    ));
        }
        
        // Nếu có status, tìm theo status
        if (status != null) {
            return movieRepository.findByStatus(status)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList())
                    .stream()
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                int start = (int) pageable.getOffset();
                                int end = Math.min((start + pageable.getPageSize()), list.size());
                                return new org.springframework.data.domain.PageImpl<>(
                                        list.subList(start, end),
                                        pageable,
                                        list.size()
                                );
                            }
                    ));
        }
        
        // Nếu không có filter, trả về tất cả
        return getAllMovies(pageable);
    }
    
    /**
     * Tạo movie mới (chỉ Admin)
     */
    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        checkAdminRole();
        
        // Validation: releaseDate phải trước endDate
        if (request.getReleaseDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày khởi chiếu phải trước ngày kết thúc");
        }
        
        // Tạo Movie mới
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
        return convertToResponse(savedMovie);
    }
    
    /**
     * Cập nhật movie (chỉ Admin)
     */
    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        checkAdminRole();
        
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie không tồn tại với id: " + id));
        
        // Validation: releaseDate phải trước endDate
        if (request.getReleaseDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày khởi chiếu phải trước ngày kết thúc");
        }
        
        // Cập nhật thông tin
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setPoster(request.getPoster());
        movie.setTrailer(request.getTrailer());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        }
        if (request.getAgeRating() != null) {
            movie.setAgeRating(request.getAgeRating());
        }
        
        Movie updatedMovie = movieRepository.save(movie);
        return convertToResponse(updatedMovie);
    }
    
    /**
     * Xóa movie (chỉ Admin)
     */
    @Transactional
    public void deleteMovie(Long id) {
        checkAdminRole();
        
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie không tồn tại với id: " + id);
        }
        
        movieRepository.deleteById(id);
    }
    
    /**
     * Convert Movie entity sang MovieResponse DTO
     */
    private MovieResponse convertToResponse(Movie movie) {
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

