package com.cinema.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_refreshments",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_booking_refreshment",
           columnNames = {"booking_id", "refreshment_id"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRefreshment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refreshment_id", nullable = false)
    private Refreshment refreshment;

    @Column(nullable = false)
    private Integer quantity; // Số lượng

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // Tổng tiền = price × quantity

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

