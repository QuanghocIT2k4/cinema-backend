package com.cinema.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "refreshments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Refreshment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name; // Tên sản phẩm (ví dụ: Bắp rang bơ, Coca Cola, Combo 1)

    @Column(length = 255)
    private String picture; // Link ảnh sản phẩm

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Giá (VNĐ)

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true; // Còn bán hay không

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "refreshment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingRefreshment> bookingRefreshments;
}

