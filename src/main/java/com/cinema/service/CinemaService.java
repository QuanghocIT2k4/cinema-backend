package com.cinema.service;

import com.cinema.model.dto.CinemaCreateRequest;
import com.cinema.model.dto.CinemaResponse;
import com.cinema.model.dto.CinemaUpdateRequest;
import com.cinema.model.dto.RoomResponse;
import com.cinema.model.entity.Cinema;
import com.cinema.model.entity.Room;
import com.cinema.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic CRUD Cinema
 */
@Service
@RequiredArgsConstructor
public class CinemaService {
    
    private final CinemaRepository cinemaRepository;
    
    /**
     * Lấy danh sách tất cả Cinema
     */
    public List<CinemaResponse> getAllCinemas() {
        List<Cinema> cinemas = cinemaRepository.findAll();
        return cinemas.stream()
            .map(this::convertToCinemaResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy chi tiết 1 Cinema (bao gồm danh sách rooms)
     */
    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cinema không tồn tại"));
        return convertToCinemaResponse(cinema);
    }
    
    /**
     * Tạo Cinema mới (Admin only)
     */
    @Transactional
    public CinemaResponse createCinema(CinemaCreateRequest request) {
        Cinema cinema = new Cinema();
        cinema.setName(request.getName());
        cinema.setAddress(request.getAddress());
        cinema.setPhone(request.getPhone());
        cinema.setEmail(request.getEmail());
        
        Cinema savedCinema = cinemaRepository.save(cinema);
        return convertToCinemaResponse(savedCinema);
    }
    
    /**
     * Cập nhật Cinema (Admin only)
     */
    @Transactional
    public CinemaResponse updateCinema(Long id, CinemaUpdateRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cinema không tồn tại"));
        
        if (request.getName() != null) {
            cinema.setName(request.getName());
        }
        if (request.getAddress() != null) {
            cinema.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            cinema.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            cinema.setEmail(request.getEmail());
        }
        
        Cinema updatedCinema = cinemaRepository.save(cinema);
        return convertToCinemaResponse(updatedCinema);
    }
    
    /**
     * Xóa Cinema (Admin only)
     */
    @Transactional
    public void deleteCinema(Long id) {
        if (!cinemaRepository.existsById(id)) {
            throw new RuntimeException("Cinema không tồn tại");
        }
        cinemaRepository.deleteById(id);
    }
    
    /**
     * Convert Cinema entity sang CinemaResponse DTO
     */
    private CinemaResponse convertToCinemaResponse(Cinema cinema) {
        CinemaResponse response = new CinemaResponse();
        response.setId(cinema.getId());
        response.setName(cinema.getName());
        response.setAddress(cinema.getAddress());
        response.setPhone(cinema.getPhone());
        response.setEmail(cinema.getEmail());
        response.setCreatedAt(cinema.getCreatedAt());
        response.setUpdatedAt(cinema.getUpdatedAt());
        
        // Convert rooms nếu có
        if (cinema.getRooms() != null) {
            List<RoomResponse> roomResponses = cinema.getRooms().stream()
                .map(this::convertToRoomResponse)
                .collect(Collectors.toList());
            response.setRooms(roomResponses);
        }
        
        return response;
    }
    
    /**
     * Convert Room entity sang RoomResponse DTO (helper method)
     */
    private RoomResponse convertToRoomResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setCinemaId(room.getCinema().getId());
        response.setCinemaName(room.getCinema().getName());
        response.setRoomNumber(room.getRoomNumber());
        response.setTotalRows(room.getTotalRows());
        response.setTotalCols(room.getTotalCols());
        response.setTotalSeats(room.getTotalSeats());
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());
        // Không set seats ở đây để tránh circular reference
        return response;
    }
}

