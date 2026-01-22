package com.cinema.service;

import com.cinema.model.dto.request.ShowtimeRequest;
import com.cinema.model.dto.response.ShowtimeResponse;
import com.cinema.model.entity.Movie;
import com.cinema.model.entity.Room;
import com.cinema.model.entity.Showtime;
import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.MovieStatus;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.ShowtimeRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service xử lý logic CRUD Showtime + kiểm tra xung đột
 */
@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    private void checkAdminRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails.getUser().getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Chỉ Admin mới có quyền thực hiện thao tác này");
        }
    }

    public Page<ShowtimeResponse> getAllShowtimes(Pageable pageable) {
        return showtimeRepository.findAll(pageable).map(this::convertToResponse);
    }

    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime không tồn tại với id: " + id));
        return convertToResponse(showtime);
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovieId(Long movieId) {
        // JOIN FETCH đã load Room và Cinema
        // Chỉ trả về showtime trong tương lai (ít nhất 30 phút từ bây giờ)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minStartTime = now.plusMinutes(30);
        
        return showtimeRepository.findByMovie_Id(movieId).stream()
                .filter(s -> s.getStartTime().isAfter(minStartTime))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        // JOIN FETCH đã load Room và Cinema, không cần @Transactional nhưng thêm để an toàn
        return showtimeRepository.findByStartTimeBetween(startOfDay, endOfDay).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        checkAdminRole();

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie không tồn tại với id: " + request.getMovieId()));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room không tồn tại với id: " + request.getRoomId()));

        // Nếu không truyền endTime thì tự động tính theo thời lượng phim
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        if (endTime == null) {
            if (movie.getDuration() == null || movie.getDuration() <= 0) {
                throw new RuntimeException("Không thể tự tính end time vì thời lượng phim không hợp lệ");
            }
            endTime = startTime.plusMinutes(movie.getDuration());
        }

        validateTimes(startTime, endTime);

        // Không cho phép tạo suất chiếu cho phim đã kết thúc
        if (movie.getStatus() == MovieStatus.ENDED) {
            throw new RuntimeException("Phim đã kết thúc, không thể tạo suất chiếu mới");
        }

        // Ngày suất chiếu phải nằm trong khoảng [releaseDate, endDate] của phim
        LocalDate showDate = startTime.toLocalDate();
        if (showDate.isBefore(movie.getReleaseDate()) || showDate.isAfter(movie.getEndDate())) {
            throw new RuntimeException("Thời gian suất chiếu phải nằm trong khoảng thời gian phát hành của phim");
        }

        // check conflict
        validateNoConflict(null, room.getId(), request.getStartTime(), request.getEndTime());

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setPrice(request.getPrice());

        Showtime saved = showtimeRepository.save(showtime);
        return convertToResponse(saved);
    }

    @Transactional
    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        checkAdminRole();

        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime không tồn tại với id: " + id));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie không tồn tại với id: " + request.getMovieId()));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room không tồn tại với id: " + request.getRoomId()));

        // Nếu không truyền endTime thì tự động tính theo thời lượng phim
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        if (endTime == null) {
            if (movie.getDuration() == null || movie.getDuration() <= 0) {
                throw new RuntimeException("Không thể tự tính end time vì thời lượng phim không hợp lệ");
            }
            endTime = startTime.plusMinutes(movie.getDuration());
        }

        validateTimes(startTime, endTime);

        // Không cho phép cập nhật suất chiếu sang phim đã kết thúc
        if (movie.getStatus() == MovieStatus.ENDED) {
            throw new RuntimeException("Phim đã kết thúc, không thể gán suất chiếu cho phim này");
        }

        // Ngày suất chiếu phải nằm trong khoảng [releaseDate, endDate] của phim
        LocalDate showDate = startTime.toLocalDate();
        if (showDate.isBefore(movie.getReleaseDate()) || showDate.isAfter(movie.getEndDate())) {
            throw new RuntimeException("Thời gian suất chiếu phải nằm trong khoảng thời gian phát hành của phim");
        }

        // check conflict (exclude itself)
        validateNoConflict(id, room.getId(), request.getStartTime(), request.getEndTime());

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setPrice(request.getPrice());

        Showtime updated = showtimeRepository.save(showtime);
        return convertToResponse(updated);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        checkAdminRole();
        if (!showtimeRepository.existsById(id)) {
            throw new RuntimeException("Showtime không tồn tại với id: " + id);
        }
        showtimeRepository.deleteById(id);
    }

    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new RuntimeException("Start time phải trước end time");
        }
        // Optional: tránh tạo suất chiếu quá khứ
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new RuntimeException("Start time không được ở quá khứ");
        }
    }

    private void validateNoConflict(Long excludeShowtimeId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Showtime> conflicts = showtimeRepository.findConflictingShowtimes(roomId, startTime, endTime);
        boolean hasConflict = conflicts.stream().anyMatch(s -> excludeShowtimeId == null || !s.getId().equals(excludeShowtimeId));
        if (hasConflict) {
            throw new RuntimeException("Suất chiếu bị xung đột thời gian trong cùng phòng");
        }
    }

    private ShowtimeResponse convertToResponse(Showtime showtime) {
        ShowtimeResponse res = new ShowtimeResponse();
        res.setId(showtime.getId());

        if (showtime.getMovie() != null) {
            res.setMovieId(showtime.getMovie().getId());
            res.setMovieTitle(showtime.getMovie().getTitle());
        }
        if (showtime.getRoom() != null) {
            res.setRoomId(showtime.getRoom().getId());
            res.setRoomNumber(showtime.getRoom().getRoomNumber());
            if (showtime.getRoom().getCinema() != null) {
                res.setCinemaId(showtime.getRoom().getCinema().getId());
                res.setCinemaName(showtime.getRoom().getCinema().getName());
            }
        }

        res.setStartTime(showtime.getStartTime());
        res.setEndTime(showtime.getEndTime());
        res.setPrice(showtime.getPrice());
        res.setCreatedAt(showtime.getCreatedAt());
        res.setUpdatedAt(showtime.getUpdatedAt());
        return res;
    }
}








