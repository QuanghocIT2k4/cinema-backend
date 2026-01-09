package com.cinema.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rooms", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_cinema_room",
           columnNames = {"cinema_id", "room_number"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "room_number", nullable = false, length = 10)
    private String roomNumber;

    // Dùng tên cột an toàn, tránh từ khóa SQL (ROWS, COLUMNS, ...)
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows; // Số hàng ghế

    @Column(name = "total_cols", nullable = false)
    private Integer totalCols; // Số ghế mỗi hàng

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats; // rows × cols

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Showtime> showtimes;
}

