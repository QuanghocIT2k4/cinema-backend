package com.cinema.repository;

import com.cinema.model.entity.Movie;
import com.cinema.model.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    // Tìm phim theo status
    List<Movie> findByStatus(MovieStatus status);
    
    // Tìm phim theo thể loại
    List<Movie> findByGenre(String genre);
    
    // Tìm phim theo tên (contains)
    List<Movie> findByTitleContaining(String keyword);
    
    // Tìm phim đang chiếu (status = NOW_SHOWING)
    List<Movie> findByStatusAndReleaseDateLessThanEqual(MovieStatus status, LocalDate date);
    
    // Tìm phim sắp chiếu (status = COMING_SOON)
    List<Movie> findByStatusAndReleaseDateGreaterThan(MovieStatus status, LocalDate date);
}

