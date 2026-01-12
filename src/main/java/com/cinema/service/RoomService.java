package com.cinema.service;

import com.cinema.model.dto.RoomCreateRequest;
import com.cinema.model.dto.RoomResponse;
import com.cinema.model.dto.RoomUpdateRequest;
import com.cinema.model.dto.SeatResponse;
import com.cinema.model.entity.Cinema;
import com.cinema.model.entity.Room;
import com.cinema.model.entity.Seat;
import com.cinema.model.enums.SeatType;
import com.cinema.repository.CinemaRepository;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * Lấy danh sách Room (có thể filter theo cinemaId)
     */
    public List<RoomResponse> getAllRooms(Long cinemaId) {
        List<Room> rooms;
        if (cinemaId != null) {
            rooms = roomRepository.findByCinemaId(cinemaId);
        } else {
            rooms = roomRepository.findAll();
        }
        
        return rooms.stream()
            .map(this::convertToRoomResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy chi tiết 1 Room (bao gồm danh sách seats)
     */
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room không tồn tại"));
        return convertToRoomResponse(room);
    }
    
    /**
     * Tạo Room mới và tự động tạo ghế (Admin only)
     * Logic: Tự động tạo ghế dựa vào totalRows × totalCols
     */
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        // Tìm Cinema
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
            .orElseThrow(() -> new RuntimeException("Cinema không tồn tại"));
        
        // Kiểm tra số phòng không trùng trong cùng rạp
        if (roomRepository.existsByCinemaIdAndRoomNumber(request.getCinemaId(), request.getRoomNumber())) {
            throw new RuntimeException("Số phòng đã tồn tại trong rạp này");
        }
        
        // Tạo Room mới
        Room room = new Room();
        room.setCinema(cinema);
        room.setRoomNumber(request.getRoomNumber());
        room.setTotalRows(request.getTotalRows());
        room.setTotalCols(request.getTotalCols());
        room.setTotalSeats(request.getTotalRows() * request.getTotalCols());
        
        Room savedRoom = roomRepository.save(room);
        
        // Tự động tạo ghế
        createSeatsForRoom(savedRoom, request.getTotalRows(), request.getTotalCols());
        
        // Reload room để có seats
        savedRoom = roomRepository.findById(savedRoom.getId())
            .orElseThrow(() -> new RuntimeException("Room không tồn tại"));
        
        return convertToRoomResponse(savedRoom);
    }
    
    /**
     * Tự động tạo ghế cho Room
     * Format: A1, A2, ..., B1, B2, ...
     * Hàng cuối cùng (2 hàng cuối) là VIP, còn lại là NORMAL
     */
    private void createSeatsForRoom(Room room, Integer totalRows, Integer totalCols) {
        List<Seat> seats = new ArrayList<>();
        
        for (int row = 0; row < totalRows; row++) {
            char rowChar = (char) ('A' + row); // A, B, C, ...
            
            // Xác định loại ghế: 2 hàng cuối là VIP
            SeatType seatType = (row >= totalRows - 2) ? SeatType.VIP : SeatType.NORMAL;
            
            for (int col = 1; col <= totalCols; col++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatNumber(String.valueOf(rowChar) + col); // A1, A2, ...
                seat.setRow(String.valueOf(rowChar));
                seat.setCol(col);
                seat.setType(seatType);
                
                seats.add(seat);
            }
        }
        
        // Lưu tất cả ghế vào database
        seatRepository.saveAll(seats);
    }
    
    /**
     * Cập nhật Room (Admin only)
     * Lưu ý: Nếu thay đổi rows/cols, cần xóa ghế cũ và tạo lại
     */
    @Transactional
    public RoomResponse updateRoom(Long id, RoomUpdateRequest request) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room không tồn tại"));
        
        boolean needRecreateSeats = false;
        Integer oldRows = room.getTotalRows();
        Integer oldCols = room.getTotalCols();
        
        // Kiểm tra số phòng không trùng (nếu thay đổi)
        if (request.getRoomNumber() != null && !request.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByCinemaIdAndRoomNumber(room.getCinema().getId(), request.getRoomNumber())) {
                throw new RuntimeException("Số phòng đã tồn tại trong rạp này");
            }
            room.setRoomNumber(request.getRoomNumber());
        }
        
        // Cập nhật rows/cols nếu có thay đổi
        if (request.getTotalRows() != null && !request.getTotalRows().equals(oldRows)) {
            room.setTotalRows(request.getTotalRows());
            needRecreateSeats = true;
        }
        if (request.getTotalCols() != null && !request.getTotalCols().equals(oldCols)) {
            room.setTotalCols(request.getTotalCols());
            needRecreateSeats = true;
        }
        
        // Tính lại totalSeats
        if (request.getTotalRows() != null || request.getTotalCols() != null) {
            Integer newRows = request.getTotalRows() != null ? request.getTotalRows() : oldRows;
            Integer newCols = request.getTotalCols() != null ? request.getTotalCols() : oldCols;
            room.setTotalSeats(newRows * newCols);
        }
        
        Room updatedRoom = roomRepository.save(room);
        
        // Nếu thay đổi rows/cols, xóa ghế cũ và tạo lại
        if (needRecreateSeats) {
            // Xóa ghế cũ
            List<Seat> oldSeats = seatRepository.findByRoomId(id);
            seatRepository.deleteAll(oldSeats);
            
            // Tạo ghế mới
            createSeatsForRoom(updatedRoom, updatedRoom.getTotalRows(), updatedRoom.getTotalCols());
        }
        
        // Reload room để có seats mới
        updatedRoom = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room không tồn tại"));
        
        return convertToRoomResponse(updatedRoom);
    }
    
    /**
     * Xóa Room (Admin only)
     */
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room không tồn tại");
        }
        // Xóa Room sẽ tự động xóa seats (cascade)
        roomRepository.deleteById(id);
    }
    
    /**
     * Convert Room entity sang RoomResponse DTO
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
        
        // Convert seats nếu có
        if (room.getSeats() != null && !room.getSeats().isEmpty()) {
            List<SeatResponse> seatResponses = room.getSeats().stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
            response.setSeats(seatResponses);
        }
        
        return response;
    }
    
    /**
     * Convert Seat entity sang SeatResponse DTO
     */
    private SeatResponse convertToSeatResponse(Seat seat) {
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

