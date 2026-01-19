package com.cinema.model.dto.request;

import com.cinema.model.enums.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request tạo/cập nhật Movie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {
    
    @NotBlank(message = "Tên phim không được để trống")
    @Size(max = 200, message = "Tên phim không được vượt quá 200 ký tự")
    private String title;
    
    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;
    
    @Size(max = 50, message = "Thể loại không được vượt quá 50 ký tự")
    private String genre;
    
    @NotNull(message = "Thời lượng không được để trống")
    @Positive(message = "Thời lượng phải lớn hơn 0")
    private Integer duration; // phút
    
    @Size(max = 255, message = "Link poster không được vượt quá 255 ký tự")
    private String poster;
    
    @Size(max = 255, message = "Link trailer không được vượt quá 255 ký tự")
    private String trailer; // Link YouTube
    
    @NotNull(message = "Ngày khởi chiếu không được để trống")
    private LocalDate releaseDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    private MovieStatus status;
    
    @Size(max = 10, message = "Độ tuổi không được vượt quá 10 ký tự")
    private String ageRating; // G, PG, PG-13, R
}








