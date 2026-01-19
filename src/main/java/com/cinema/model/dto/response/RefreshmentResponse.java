package com.cinema.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO cho Refreshment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshmentResponse {
    private Long id;
    private String name;
    private String picture;
    private BigDecimal price;
    private Boolean isCurrent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


