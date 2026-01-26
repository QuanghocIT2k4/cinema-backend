package com.cinema.repository;

import com.cinema.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovie_IdOrderByCreatedAtDesc(Long movieId);
}

