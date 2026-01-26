package com.cinema.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * MovieActor: bảng lưu danh sách diễn viên cho từng phim.
 *
 * Thiết kế đơn giản theo đúng nhu cầu hiện tại:
 * - Mỗi record là 1 diễn viên thuộc về 1 movie.
 * - Lưu luôn name + avatarUrl, KHÔNG tách bảng Actor riêng.
 * - Nếu sau này cần tái sử dụng Actor cho nhiều phim, có thể refactor:
 *   + Tạo bảng Actor riêng
 *   + Đổi MovieActor thành bảng trung gian many-to-many.
 */
@Entity
@Table(name = "movie_actors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieActor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ N-1: nhiều diễn viên thuộc về 1 movie.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * Tên diễn viên (ví dụ: "Robert Downey Jr.")
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Ảnh đại diện (URL)
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


