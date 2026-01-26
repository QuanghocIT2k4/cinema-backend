package com.cinema.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response trả về thông tin Review
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String authorName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

