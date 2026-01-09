package com.cinema.repository;

import com.cinema.model.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    // Tìm rạp theo tên (contains)
    List<Cinema> findByNameContaining(String keyword);
}

