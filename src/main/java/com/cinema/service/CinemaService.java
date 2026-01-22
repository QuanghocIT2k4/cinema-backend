package com.cinema.service;

import com.cinema.model.dto.request.CinemaRequest;
import com.cinema.model.dto.response.CinemaResponse;
import com.cinema.model.entity.Cinema;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.CinemaRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý logic CRUD Cinema
 */
@Service
@RequiredArgsConstructor
public class CinemaService {
    
    private final CinemaRepository cinemaRepository;
    
    /**
     * Kiểm tra user hiện tại có phải Admin không
     */
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
    
    /**
     * Lấy tất cả cinemas (có phân trang)
     */
    public Page<CinemaResponse> getAllCinemas(Pageable pageable) {
        return cinemaRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Lấy cinema theo ID
     */
    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cinema không tồn tại với id: " + id));
        return convertToResponse(cinema);
    }
    
    /**
     * Tạo cinema mới (chỉ Admin)
     */
    @Transactional
    public CinemaResponse createCinema(CinemaRequest request) {
        checkAdminRole();
        
        // Tạo Cinema mới
        Cinema cinema = new Cinema();
        cinema.setName(request.getName());
        cinema.setAddress(request.getAddress());
        cinema.setPhone(request.getPhone());
        cinema.setEmail(request.getEmail());
        
        Cinema savedCinema = cinemaRepository.save(cinema);
        return convertToResponse(savedCinema);
    }
    
    /**
     * Cập nhật cinema (chỉ Admin)
     */
    @Transactional
    public CinemaResponse updateCinema(Long id, CinemaRequest request) {
        checkAdminRole();
        
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cinema không tồn tại với id: " + id));
        
        // Cập nhật thông tin
        cinema.setName(request.getName());
        cinema.setAddress(request.getAddress());
        if (request.getPhone() != null) {
            cinema.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            cinema.setEmail(request.getEmail());
        }
        
        Cinema updatedCinema = cinemaRepository.save(cinema);
        return convertToResponse(updatedCinema);
    }
    
    /**
     * Xóa cinema (chỉ Admin)
     */
    @Transactional
    public void deleteCinema(Long id) {
        checkAdminRole();
        
        if (!cinemaRepository.existsById(id)) {
            throw new RuntimeException("Cinema không tồn tại với id: " + id);
        }
        
        // Kiểm tra cinema có phòng chiếu không (nếu có thì không cho xóa)
        Cinema cinema = cinemaRepository.findById(id).orElseThrow();
        if (cinema.getRooms() != null && !cinema.getRooms().isEmpty()) {
            throw new RuntimeException("Không thể xóa rạp đang có phòng chiếu. Vui lòng xóa tất cả phòng chiếu trước.");
        }
        
        cinemaRepository.deleteById(id);
    }
    
    /**
     * Convert Cinema entity sang CinemaResponse DTO
     */
    private CinemaResponse convertToResponse(Cinema cinema) {
        CinemaResponse response = new CinemaResponse();
        response.setId(cinema.getId());
        response.setName(cinema.getName());
        response.setAddress(cinema.getAddress());
        response.setPhone(cinema.getPhone());
        response.setEmail(cinema.getEmail());
        response.setCreatedAt(cinema.getCreatedAt());
        response.setUpdatedAt(cinema.getUpdatedAt());
        return response;
    }
}














