package com.cinema.model.dto;

import com.cinema.model.enums.MovieStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request cập nhật Movie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieUpdateRequest {
    
    @Size(max = 200, message = "Tên phim không được vượt quá 200 ký tự")
    private String title;
    
    private String description;
    
    @Size(max = 50, message = "Thể loại không được vượt quá 50 ký tự")
    private String genre;
    
    @Positive(message = "Thời lượng phải lớn hơn 0")
    private Integer duration;
    
    @Size(max = 255, message = "Link poster không được vượt quá 255 ký tự")
    private String poster;
    
    @Size(max = 255, message = "Link trailer không được vượt quá 255 ký tự")
    private String trailer;
    
    private LocalDate releaseDate;
    
    private LocalDate endDate;
    
    private MovieStatus status;
    
    @Size(max = 10, message = "Độ tuổi cho phép không được vượt quá 10 ký tự")
    private String ageRating;
}

