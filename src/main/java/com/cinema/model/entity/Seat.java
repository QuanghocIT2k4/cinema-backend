package com.cinema.model.entity;

import com.cinema.model.enums.SeatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seats",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_room_seat",
           columnNames = {"room_id", "seat_number"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber; // A1, A2, B1, ...

    @Column(name = "seat_row", nullable = false, length = 5)
    private String row; // A, B, C, ...

    @Column(nullable = false)
    private Integer col; // 1, 2, 3, ...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType type = SeatType.NORMAL;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;
}

