package com.cinema.service;

import com.cinema.model.dto.request.RoomRequest;
import com.cinema.model.dto.response.RoomResponse;
import com.cinema.model.dto.response.SeatResponse;
import com.cinema.model.entity.Cinema;
import com.cinema.model.entity.Room;
import com.cinema.model.entity.Seat;
import com.cinema.model.enums.SeatType;
import com.cinema.model.enums.UserRole;
import com.cinema.repository.CinemaRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý logic CRUD Room
 */
@Service
@RequiredArgsConstructor
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;
    private final SeatRepository seatRepository;
    
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
     * Lấy tất cả rooms (có phân trang)
     */
    public Page<RoomResponse> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Lấy rooms theo cinema ID
     */
    public List<RoomResponse> getRoomsByCinemaId(Long cinemaId) {
        return roomRepository.findByCinemaId(cinemaId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    /**
     * Lấy room theo ID
     */
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room không tồn tại với id: " + id));
        return convertToResponse(room);
    }

    /**
     * Lấy danh sách ghế theo room ID (public, không cần admin)
     */
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByRoomId(Long roomId) {
        // Kiểm tra room tồn tại
        if (!roomRepository.existsById(roomId)) {
            throw new RuntimeException("Room không tồn tại với id: " + roomId);
        }
        
        List<Seat> seats = seatRepository.findByRoomId(roomId);
        return seats.stream()
                .map(this::convertSeatToResponse)
                .toList();
    }
    
    /**
     * Tạo room mới và tự động tạo ghế (chỉ Admin)
     */
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        checkAdminRole();
        
        // Kiểm tra cinema tồn tại
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Cinema không tồn tại với id: " + request.getCinemaId()));
        
        // Validation: số phòng không trùng trong cùng rạp
        if (roomRepository.existsByCinemaIdAndRoomNumber(request.getCinemaId(), request.getRoomNumber())) {
            throw new RuntimeException("Số phòng " + request.getRoomNumber() + " đã tồn tại trong rạp này");
        }
        
        // Tạo Room mới
        Room room = new Room();
        room.setCinema(cinema);
        room.setRoomNumber(request.getRoomNumber());
        room.setTotalRows(request.getTotalRows());
        room.setTotalCols(request.getTotalCols());
        room.setTotalSeats(request.getTotalRows() * request.getTotalCols());
        
        Room savedRoom = roomRepository.save(room);
        
        // Tự động tạo ghế dựa vào rows và cols
        createSeatsForRoom(savedRoom, request.getDefaultSeatType());
        
        // Reload room để có seats
        savedRoom = roomRepository.findById(savedRoom.getId()).orElseThrow();
        
        return convertToResponse(savedRoom);
    }
    
    /**
     * Tự động tạo ghế cho room
     * Ví dụ: rows=5, cols=10 → tạo A1-A10, B1-B10, C1-C10, D1-D10, E1-E10
     */
    private void createSeatsForRoom(Room room, SeatType defaultSeatType) {
        List<Seat> seats = new ArrayList<>();
        int rows = room.getTotalRows();
        int cols = room.getTotalCols();
        
        // Tạo hàng từ A đến Z (nếu > 26 hàng thì dùng AA, AB, ...)
        for (int row = 0; row < rows; row++) {
            String rowLetter = getRowLetter(row); // A, B, C, ... hoặc AA, AB, ...
            
            for (int col = 1; col <= cols; col++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatNumber(rowLetter + col); // A1, A2, B1, ...
                seat.setRow(rowLetter);
                seat.setCol(col);
                seat.setType(defaultSeatType != null ? defaultSeatType : SeatType.NORMAL);
                
                seats.add(seat);
            }
        }
        
        seatRepository.saveAll(seats);
    }
    
    /**
     * Chuyển số hàng thành chữ cái: 0->A, 1->B, ..., 25->Z, 26->AA, 27->AB, ...
     */
    private String getRowLetter(int rowIndex) {
        if (rowIndex < 26) {
            return String.valueOf((char) ('A' + rowIndex));
        } else {
            // Nếu > 26 hàng: AA, AB, AC, ...
            int firstLetter = (rowIndex / 26) - 1;
            int secondLetter = rowIndex % 26;
            return String.valueOf((char) ('A' + firstLetter)) + (char) ('A' + secondLetter);
        }
    }
    
    /**
     * Cập nhật room (chỉ Admin)
     * Lưu ý: Nếu thay đổi rows/cols, cần xóa ghế cũ và tạo lại
     */
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        checkAdminRole();
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room không tồn tại với id: " + id));
        
        // Kiểm tra cinema tồn tại
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Cinema không tồn tại với id: " + request.getCinemaId()));
        
        // Validation: số phòng không trùng (nếu thay đổi)
        if (!room.getRoomNumber().equals(request.getRoomNumber()) && 
            roomRepository.existsByCinemaIdAndRoomNumber(request.getCinemaId(), request.getRoomNumber())) {
            throw new RuntimeException("Số phòng " + request.getRoomNumber() + " đã tồn tại trong rạp này");
        }
        
        // Nếu thay đổi rows/cols, cần xóa ghế cũ và tạo lại
        boolean needRecreateSeats = !room.getTotalRows().equals(request.getTotalRows()) || 
                                   !room.getTotalCols().equals(request.getTotalCols());
        
        if (needRecreateSeats) {
            // Xóa tất cả ghế cũ
            seatRepository.deleteAll(room.getSeats());
        }
        
        // Cập nhật thông tin
        room.setCinema(cinema);
        room.setRoomNumber(request.getRoomNumber());
        room.setTotalRows(request.getTotalRows());
        room.setTotalCols(request.getTotalCols());
        room.setTotalSeats(request.getTotalRows() * request.getTotalCols());
        
        Room updatedRoom = roomRepository.save(room);
        
        // Tạo lại ghế nếu cần
        if (needRecreateSeats) {
            createSeatsForRoom(updatedRoom, request.getDefaultSeatType());
        }
        
        // Reload room để có seats
        updatedRoom = roomRepository.findById(updatedRoom.getId()).orElseThrow();
        
        return convertToResponse(updatedRoom);
    }
    
    /**
     * Xóa room (chỉ Admin)
     */
    @Transactional
    public void deleteRoom(Long id) {
        checkAdminRole();
        
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room không tồn tại với id: " + id));
        
        // Kiểm tra room có showtime không (nếu có thì không cho xóa)
        if (room.getShowtimes() != null && !room.getShowtimes().isEmpty()) {
            throw new RuntimeException("Không thể xóa phòng đang có suất chiếu. Vui lòng xóa tất cả suất chiếu trước.");
        }
        
        // Xóa tất cả ghế (cascade sẽ tự động xóa)
        seatRepository.deleteAll(room.getSeats());
        
        roomRepository.deleteById(id);
    }
    
    /**
     * Convert Room entity sang RoomResponse DTO
     */
    private RoomResponse convertToResponse(Room room) {
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
        return response;
    }

    /**
     * Convert Seat entity sang SeatResponse DTO
     */
    private SeatResponse convertSeatToResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setId(seat.getId());
        response.setRoomId(seat.getRoom().getId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setRow(seat.getRow());
        response.setCol(seat.getCol());
        response.setType(seat.getType());
        response.setCreatedAt(seat.getCreatedAt());
        response.setUpdatedAt(seat.getUpdatedAt());
        return response;
    }
}













