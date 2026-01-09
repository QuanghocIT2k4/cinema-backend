package com.cinema.repository;

import com.cinema.model.entity.Refreshment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshmentRepository extends JpaRepository<Refreshment, Long> {
    // Tìm đồ ăn/đồ uống còn bán
    List<Refreshment> findByIsCurrentTrue();
    
    // Tìm đồ ăn/đồ uống theo tên
    List<Refreshment> findByNameContaining(String keyword);
}

