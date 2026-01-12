package com.cinema.model.dto;

import com.cinema.model.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho response trả về thông tin Movie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer duration;
    private String poster;
    private String trailer;
    private LocalDate releaseDate;
    private LocalDate endDate;
    private MovieStatus status;
    private String ageRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

