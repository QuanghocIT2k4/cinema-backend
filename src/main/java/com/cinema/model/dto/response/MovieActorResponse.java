package com.cinema.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response trả về thông tin MovieActor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieActorResponse {
    private Long id;
    private String name;
    private String avatarUrl;
}

