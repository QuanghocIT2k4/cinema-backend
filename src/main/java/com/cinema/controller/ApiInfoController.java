package com.cinema.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller hiển thị danh sách tất cả API endpoints
 */
@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = new LinkedHashMap<>();
        apiInfo.put("name", "Cinema Booking System API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("baseUrl", "/api");
        
        Map<String, Object> endpoints = new LinkedHashMap<>();
        
        // Auth APIs
        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("POST /api/auth/register", "Đăng ký tài khoản Customer");
        auth.put("POST /api/auth/login", "Đăng nhập và nhận JWT token");
        auth.put("GET /api/auth/me", "Lấy thông tin user hiện tại (JWT required)");
        auth.put("PUT /api/auth/change-password", "Đổi mật khẩu (JWT required)");
        endpoints.put("Auth APIs", auth);
        
        // Movie APIs
        Map<String, Object> movies = new LinkedHashMap<>();
        movies.put("GET /api/movies", "Lấy danh sách phim (phân trang, public)");
        movies.put("GET /api/movies/search", "Tìm kiếm phim (keyword, genre, status, public)");
        movies.put("GET /api/movies/{id}", "Lấy chi tiết phim (public)");
        movies.put("POST /api/movies", "Tạo phim mới (Admin only)");
        movies.put("PUT /api/movies/{id}", "Cập nhật phim (Admin only)");
        movies.put("DELETE /api/movies/{id}", "Xóa phim (Admin only)");
        endpoints.put("Movie APIs", movies);
        
        // Cinema APIs
        Map<String, Object> cinemas = new LinkedHashMap<>();
        cinemas.put("GET /api/cinemas", "Lấy danh sách rạp (phân trang, public)");
        cinemas.put("GET /api/cinemas/{id}", "Lấy chi tiết rạp (public)");
        cinemas.put("POST /api/cinemas", "Tạo rạp mới (Admin only)");
        cinemas.put("PUT /api/cinemas/{id}", "Cập nhật rạp (Admin only)");
        cinemas.put("DELETE /api/cinemas/{id}", "Xóa rạp (Admin only)");
        endpoints.put("Cinema APIs", cinemas);
        
        // Room APIs
        Map<String, Object> rooms = new LinkedHashMap<>();
        rooms.put("GET /api/rooms", "Lấy danh sách phòng (phân trang, public)");
        rooms.put("GET /api/rooms/cinema/{cinemaId}", "Lấy danh sách phòng theo rạp (public)");
        rooms.put("GET /api/rooms/{id}", "Lấy chi tiết phòng (public)");
        rooms.put("POST /api/rooms", "Tạo phòng chiếu mới (Admin only)");
        rooms.put("PUT /api/rooms/{id}", "Cập nhật phòng (Admin only)");
        rooms.put("DELETE /api/rooms/{id}", "Xóa phòng (Admin only)");
        endpoints.put("Room APIs", rooms);
        
        // Showtime APIs
        Map<String, Object> showtimes = new LinkedHashMap<>();
        showtimes.put("GET /api/showtimes", "Lấy danh sách suất chiếu (phân trang, public)");
        showtimes.put("GET /api/showtimes/{id}", "Lấy chi tiết suất chiếu (public)");
        showtimes.put("GET /api/showtimes/movie/{movieId}", "Lấy suất chiếu theo phim (public)");
        showtimes.put("GET /api/showtimes/date/{date}", "Lấy suất chiếu theo ngày yyyy-MM-dd (public)");
        showtimes.put("POST /api/showtimes", "Tạo suất chiếu mới (Admin only)");
        showtimes.put("PUT /api/showtimes/{id}", "Cập nhật suất chiếu (Admin only)");
        showtimes.put("DELETE /api/showtimes/{id}", "Xóa suất chiếu (Admin only)");
        endpoints.put("Showtime APIs", showtimes);
        
        // Booking APIs
        Map<String, Object> bookings = new LinkedHashMap<>();
        bookings.put("POST /api/bookings", "Tạo booking (đặt vé, Customer JWT required)");
        bookings.put("GET /api/bookings", "Lấy danh sách booking (phân trang, Customer/Admin)");
        bookings.put("GET /api/bookings/{id}", "Lấy chi tiết booking (Customer/Admin)");
        bookings.put("PUT /api/bookings/{id}/confirm", "Xác nhận thanh toán (Admin only)");
        bookings.put("PUT /api/bookings/{id}/cancel", "Hủy booking (Customer/Admin)");
        bookings.put("GET /api/bookings/showtime/{showtimeId}/seats", "Lấy danh sách ghế đã đặt của showtime (public)");
        endpoints.put("Booking APIs", bookings);
        
        // Ticket APIs
        Map<String, Object> tickets = new LinkedHashMap<>();
        tickets.put("GET /api/tickets/booking/{bookingId}", "Lấy danh sách vé của booking (Customer/Admin)");
        endpoints.put("Ticket APIs", tickets);
        
        // Refreshment APIs
        Map<String, Object> refreshments = new LinkedHashMap<>();
        refreshments.put("GET /api/refreshments", "Lấy danh sách đồ ăn/đồ uống đang bán (public)");
        refreshments.put("POST /api/refreshments", "Tạo refreshment mới (Admin only)");
        endpoints.put("Refreshment APIs", refreshments);
        
        // User APIs (Admin)
        Map<String, Object> users = new LinkedHashMap<>();
        users.put("GET /api/users", "Lấy danh sách user (phân trang, Admin only)");
        users.put("GET /api/users/{id}", "Lấy chi tiết user (Admin/Customer chỉ xem được mình)");
        users.put("POST /api/users", "Tạo user mới (Admin only)");
        users.put("PUT /api/users/{id}", "Cập nhật user (Admin/Customer chỉ sửa được mình)");
        users.put("DELETE /api/users/{id}", "Xóa user (Admin only)");
        endpoints.put("User APIs", users);
        
        apiInfo.put("endpoints", endpoints);
        
        Map<String, String> notes = new LinkedHashMap<>();
        notes.put("Authentication", "Các endpoint yêu cầu JWT: gửi token trong header 'Authorization: Bearer <token>'");
        notes.put("Public", "Các endpoint public không cần authentication");
        notes.put("Admin only", "Chỉ Admin mới có quyền truy cập");
        notes.put("Customer/Admin", "Customer chỉ xem/sửa được dữ liệu của mình, Admin xem/sửa được tất cả");
        apiInfo.put("notes", notes);
        
        return ResponseEntity.ok(apiInfo);
    }
}

