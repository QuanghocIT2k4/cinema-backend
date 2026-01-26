package com.cinema.service;

import com.cinema.model.dto.request.MovieRequest;
import com.cinema.model.dto.response.MovieActorResponse;
import com.cinema.model.dto.response.MovieResponse;
import com.cinema.model.dto.response.ReviewResponse;
import com.cinema.model.entity.Movie;
import com.cinema.model.entity.MovieActor;
import com.cinema.model.entity.Review;
import com.cinema.model.enums.MovieStatus;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.MovieActorRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ReviewRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý logic CRUD Movie
 */
@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    private final MovieActorRepository movieActorRepository;
    private final ReviewRepository reviewRepository;
    
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
     * Tạo Pageable với sort
     */
    public Pageable createPageable(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
            
            switch (sortBy.toLowerCase()) {
                case "releasedate":
                case "release_date":
                    sort = Sort.by(direction, "releaseDate");
                    break;
                case "title":
                    sort = Sort.by(direction, "title");
                    break;
                case "rating":
                case "agerating":
                case "age_rating":
                    sort = Sort.by(direction, "ageRating");
                    break;
                default:
                    sort = Sort.by(Sort.Direction.DESC, "releaseDate");
            }
        } else {
            sort = Sort.by(Sort.Direction.DESC, "releaseDate");
        }
        return PageRequest.of(page, size, sort);
    }
    
    /**
     * Tìm kiếm movies theo name, genre, year, rating, status
     */
    public Page<MovieResponse> searchMovies(String keyword, String genre, Integer year, String rating, MovieStatus status, Pageable pageable) {
        // Lấy tất cả movies để filter
        List<Movie> allMovies = movieRepository.findAll();
        
        // Apply filters
        List<Movie> filtered = allMovies.stream()
                .filter(movie -> {
                    // Filter by keyword
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        if (!movie.getTitle().toLowerCase().contains(keyword.trim().toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // Filter by genre (hỗ trợ multiple genres comma-separated)
                    if (genre != null && !genre.trim().isEmpty()) {
                        String[] genres = genre.split(",");
                        boolean matches = false;
                        for (String g : genres) {
                            if (movie.getGenre() != null && movie.getGenre().equalsIgnoreCase(g.trim())) {
                                matches = true;
                                break;
                            }
                        }
                        if (!matches) return false;
                    }
                    
                    // Filter by year
                    if (year != null) {
                        if (movie.getReleaseDate() != null && movie.getReleaseDate().getYear() != year) {
                            return false;
                        }
                    }
                    
                    // Filter by rating (ageRating)
                    if (rating != null && !rating.trim().isEmpty()) {
                        String ratingValue = rating.replace("+", "").trim();
                        if (movie.getAgeRating() != null) {
                            String movieRating = movie.getAgeRating().replaceAll("[^0-9]", "");
                            if (!movieRating.isEmpty()) {
                                int movieRatingInt = Integer.parseInt(movieRating);
                                int filterRatingInt = Integer.parseInt(ratingValue);
                                if (movieRatingInt < filterRatingInt) {
                                    return false;
                                }
                            }
                        }
                    }
                    
                    // Filter by status
                    if (status != null && movie.getStatus() != status) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // Apply sorting (Pageable đã có sort, nhưng cần sort lại list)
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean ascending = order.getDirection() == Sort.Direction.ASC;
            
            filtered = filtered.stream()
                    .sorted((a, b) -> {
                        int result = 0;
                        switch (property) {
                            case "releaseDate":
                                result = a.getReleaseDate().compareTo(b.getReleaseDate());
                                break;
                            case "title":
                                result = a.getTitle().compareToIgnoreCase(b.getTitle());
                                break;
                            case "ageRating":
                                String ratingA = a.getAgeRating() != null ? a.getAgeRating() : "";
                                String ratingB = b.getAgeRating() != null ? b.getAgeRating() : "";
                                result = ratingA.compareToIgnoreCase(ratingB);
                                break;
                        }
                        return ascending ? result : -result;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Convert to response
        List<MovieResponse> filteredResponses = filtered.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredResponses.size());
        List<MovieResponse> paged = start < filteredResponses.size() 
                ? filteredResponses.subList(start, end) 
                : List.of();
        
        return new org.springframework.data.domain.PageImpl<>(
                paged,
                pageable,
                filteredResponses.size()
        );
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
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        
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
        if (request.getDirector() != null) {
            movie.setDirector(request.getDirector());
        }
        if (request.getCast() != null) {
            movie.setCast(request.getCast());
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
     * Lấy danh sách diễn viên của một phim
     */
    @Transactional(readOnly = true)
    public List<MovieActorResponse> getMovieActors(Long movieId) {
        List<MovieActor> movieActors = movieActorRepository.findByMovie_Id(movieId);
        return movieActors.stream()
                .map(this::convertActorToResponse)
                .toList();
    }

    /**
     * Lấy danh sách reviews của một phim
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMovieReviews(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovie_IdOrderByCreatedAtDesc(movieId);
        return reviews.stream()
                .map(this::convertReviewToResponse)
                .toList();
    }
    
    /**
     * Convert MovieActor entity sang MovieActorResponse DTO
     */
    private MovieActorResponse convertActorToResponse(MovieActor movieActor) {
        MovieActorResponse response = new MovieActorResponse();
        response.setId(movieActor.getId());
        response.setName(movieActor.getName());
        response.setAvatarUrl(movieActor.getAvatarUrl());
        return response;
    }

    /**
     * Convert Review entity sang ReviewResponse DTO
     */
    private ReviewResponse convertReviewToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setAuthorName(review.getAuthorName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
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
        response.setDirector(movie.getDirector());
        response.setCast(movie.getCast());
        response.setCreatedAt(movie.getCreatedAt());
        response.setUpdatedAt(movie.getUpdatedAt());
        return response;
    }
}

