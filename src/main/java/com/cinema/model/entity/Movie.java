package com.cinema.model.entity;

import com.cinema.model.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String genre;

    @Column(nullable = false)
    private Integer duration; // phút

    @Column(length = 255)
    private String poster;

    @Column(length = 255)
    private String trailer; // Link YouTube

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status = MovieStatus.COMING_SOON;

    @Column(name = "age_rating", length = 10)
    private String ageRating; // G, PG, PG-13, R

    @Column(length = 255)
    private String director; // Đạo diễn

    @Column(name = "movie_cast", length = 1000)
    private String cast; // Danh sách diễn viên, ngăn cách bằng dấu phẩy

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Showtime> showtimes;

    /**
     * Danh sách diễn viên của phim (MovieActor)
     * Thiết kế hiện tại: mỗi MovieActor lưu trực tiếp name + avatarUrl.
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieActor> movieActors;

    /**
     * Danh sách review của phim
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
}

