package com.cinema.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response trả về thông tin Cinema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}















