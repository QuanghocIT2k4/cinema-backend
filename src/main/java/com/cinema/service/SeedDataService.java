package com.cinema.service;

import com.cinema.model.entity.*;
import com.cinema.model.enums.*;
import com.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service để tạo dữ liệu mẫu (Seed Data) khi khởi động ứng dụng
 * Chạy tự động khi Spring Boot start
 */
@Component
@RequiredArgsConstructor
public class SeedDataService implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) {
        // Chỉ chạy nếu database trống
        if (userRepository.count() > 0) {
            System.out.println("Database đã có dữ liệu, bỏ qua seed data.");
            return;
        }
        
        System.out.println("Bắt đầu tạo seed data...");
        
        // 1. Tạo Users (2-3 Admin, 5-10 Customer)
        createUsers();
        
        // 2. Tạo Movies (10-15 phim)
        createMovies();
        
        // 3. Tạo Cinemas (2-3 rạp)
        List<Cinema> cinemas = createCinemas();
        
        // 4. Tạo Rooms (mỗi rạp 3-5 phòng)
        createRooms(cinemas);
        
        System.out.println("Hoàn thành tạo seed data!");
    }
    
    /**
     * Tạo Users mẫu
     */
    private void createUsers() {
        List<User> users = new ArrayList<>();
        
        // Admin users (2-3)
        users.add(createUser("admin1", "admin1@cinema.com", "Admin@123", "Admin User 1", UserRole.ADMIN));
        users.add(createUser("admin2", "admin2@cinema.com", "Admin@123", "Admin User 2", UserRole.ADMIN));
        
        // Customer users (5-10)
        users.add(createUser("customer1", "customer1@example.com", "Customer@123", "Nguyễn Văn A", UserRole.CUSTOMER, "0123456789"));
        users.add(createUser("customer2", "customer2@example.com", "Customer@123", "Trần Thị B", UserRole.CUSTOMER, "0987654321"));
        users.add(createUser("customer3", "customer3@example.com", "Customer@123", "Lê Văn C", UserRole.CUSTOMER, "0912345678"));
        users.add(createUser("customer4", "customer4@example.com", "Customer@123", "Phạm Thị D", UserRole.CUSTOMER, "0923456789"));
        users.add(createUser("customer5", "customer5@example.com", "Customer@123", "Hoàng Văn E", UserRole.CUSTOMER, "0934567890"));
        users.add(createUser("customer6", "customer6@example.com", "Customer@123", "Vũ Thị F", UserRole.CUSTOMER, "0945678901"));
        users.add(createUser("customer7", "customer7@example.com", "Customer@123", "Đặng Văn G", UserRole.CUSTOMER, "0956789012"));
        
        userRepository.saveAll(users);
        System.out.println("Đã tạo " + users.size() + " users.");
    }
    
    /**
     * Helper method tạo User
     */
    private User createUser(String username, String email, String password, String fullName, UserRole role) {
        return createUser(username, email, password, fullName, role, null);
    }
    
    private User createUser(String username, String email, String password, String fullName, UserRole role, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
    
    /**
     * Tạo Movies mẫu (10-15 phim)
     */
    private void createMovies() {
        List<Movie> movies = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        // Phim đang chiếu
        movies.add(createMovie("Avengers: Endgame", "Phim siêu anh hùng Marvel", "Action", 181, 
            "https://example.com/poster/avengers.jpg", "https://youtube.com/watch?v=avengers", 
            today.minusDays(30), today.plusDays(30), MovieStatus.NOW_SHOWING, "PG-13"));
        
        movies.add(createMovie("Spider-Man: No Way Home", "Cuộc phiêu lưu của Spider-Man", "Action", 148, 
            "https://example.com/poster/spiderman.jpg", "https://youtube.com/watch?v=spiderman", 
            today.minusDays(20), today.plusDays(40), MovieStatus.NOW_SHOWING, "PG-13"));
        
        movies.add(createMovie("Dune", "Khoa học viễn tưởng", "Sci-Fi", 155, 
            "https://example.com/poster/dune.jpg", "https://youtube.com/watch?v=dune", 
            today.minusDays(15), today.plusDays(45), MovieStatus.NOW_SHOWING, "PG-13"));
        
        movies.add(createMovie("The Matrix Resurrections", "Hành động khoa học viễn tưởng", "Sci-Fi", 148, 
            "https://example.com/poster/matrix.jpg", "https://youtube.com/watch?v=matrix", 
            today.minusDays(10), today.plusDays(50), MovieStatus.NOW_SHOWING, "R"));
        
        movies.add(createMovie("No Time to Die", "Phim điệp viên 007", "Action", 163, 
            "https://example.com/poster/bond.jpg", "https://youtube.com/watch?v=bond", 
            today.minusDays(5), today.plusDays(55), MovieStatus.NOW_SHOWING, "PG-13"));
        
        // Phim sắp chiếu
        movies.add(createMovie("Avatar: The Way of Water", "Phim khoa học viễn tưởng", "Sci-Fi", 192, 
            "https://example.com/poster/avatar2.jpg", "https://youtube.com/watch?v=avatar2", 
            today.plusDays(30), today.plusDays(90), MovieStatus.COMING_SOON, "PG-13"));
        
        movies.add(createMovie("Black Panther: Wakanda Forever", "Phim siêu anh hùng", "Action", 161, 
            "https://example.com/poster/blackpanther.jpg", "https://youtube.com/watch?v=blackpanther", 
            today.plusDays(45), today.plusDays(105), MovieStatus.COMING_SOON, "PG-13"));
        
        movies.add(createMovie("Top Gun: Maverick", "Phim hành động", "Action", 130, 
            "https://example.com/poster/topgun.jpg", "https://youtube.com/watch?v=topgun", 
            today.plusDays(60), today.plusDays(120), MovieStatus.COMING_SOON, "PG-13"));
        
        movies.add(createMovie("The Batman", "Phim siêu anh hùng", "Action", 176, 
            "https://example.com/poster/batman.jpg", "https://youtube.com/watch?v=batman", 
            today.plusDays(75), today.plusDays(135), MovieStatus.COMING_SOON, "PG-13"));
        
        movies.add(createMovie("Doctor Strange in the Multiverse of Madness", "Phim siêu anh hùng", "Action", 126, 
            "https://example.com/poster/doctorstrange.jpg", "https://youtube.com/watch?v=doctorstrange", 
            today.plusDays(90), today.plusDays(150), MovieStatus.COMING_SOON, "PG-13"));
        
        // Phim đã kết thúc
        movies.add(createMovie("Titanic", "Phim tình cảm lịch sử", "Romance", 194, 
            "https://example.com/poster/titanic.jpg", "https://youtube.com/watch?v=titanic", 
            today.minusDays(365), today.minusDays(300), MovieStatus.ENDED, "PG-13"));
        
        movies.add(createMovie("The Godfather", "Phim tội phạm", "Crime", 175, 
            "https://example.com/poster/godfather.jpg", "https://youtube.com/watch?v=godfather", 
            today.minusDays(500), today.minusDays(400), MovieStatus.ENDED, "R"));
        
        movieRepository.saveAll(movies);
        System.out.println("Đã tạo " + movies.size() + " movies.");
    }
    
    /**
     * Helper method tạo Movie
     */
    private Movie createMovie(String title, String description, String genre, Integer duration,
                             String poster, String trailer, LocalDate releaseDate, LocalDate endDate,
                             MovieStatus status, String ageRating) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setGenre(genre);
        movie.setDuration(duration);
        movie.setPoster(poster);
        movie.setTrailer(trailer);
        movie.setReleaseDate(releaseDate);
        movie.setEndDate(endDate);
        movie.setStatus(status);
        movie.setAgeRating(ageRating);
        return movie;
    }
    
    /**
     * Tạo Cinemas mẫu (2-3 rạp)
     */
    private List<Cinema> createCinemas() {
        List<Cinema> cinemas = new ArrayList<>();
        
        cinemas.add(createCinema("CGV Vincom Center", "72 Lê Thánh Tôn, Quận 1, TP.HCM", "02838234567", "cgv@example.com"));
        cinemas.add(createCinema("Lotte Cinema", "469 Nguyễn Hữu Thọ, Quận 7, TP.HCM", "02837777777", "lotte@example.com"));
        cinemas.add(createCinema("Galaxy Cinema", "116 Nguyễn Du, Quận 1, TP.HCM", "02838222222", "galaxy@example.com"));
        
        cinemas = cinemaRepository.saveAll(cinemas);
        System.out.println("Đã tạo " + cinemas.size() + " cinemas.");
        return cinemas;
    }
    
    /**
     * Helper method tạo Cinema
     */
    private Cinema createCinema(String name, String address, String phone, String email) {
        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setPhone(phone);
        cinema.setEmail(email);
        return cinema;
    }
    
    /**
     * Tạo Rooms mẫu (mỗi rạp 3-5 phòng)
     */
    private void createRooms(List<Cinema> cinemas) {
        List<Room> allRooms = new ArrayList<>();
        
        for (int i = 0; i < cinemas.size(); i++) {
            Cinema cinema = cinemas.get(i);
            // Mỗi rạp có 3-5 phòng với kích thước khác nhau
            allRooms.add(createRoom(cinema, "1", 10, 15)); // 150 ghế
            allRooms.add(createRoom(cinema, "2", 12, 18)); // 216 ghế
            allRooms.add(createRoom(cinema, "3", 8, 12));  // 96 ghế
            allRooms.add(createRoom(cinema, "VIP", 6, 10)); // 60 ghế (VIP room)
            
            // Rạp đầu tiên có thêm phòng
            if (i == 0) {
                allRooms.add(createRoom(cinema, "4", 15, 20)); // 300 ghế
            }
        }
        
        allRooms = roomRepository.saveAll(allRooms);
        
        // Tạo ghế cho tất cả các phòng
        for (Room room : allRooms) {
            createSeatsForRoom(room, room.getTotalRows(), room.getTotalCols());
        }
        
        System.out.println("Đã tạo " + allRooms.size() + " rooms với tổng cộng " + 
            seatRepository.count() + " seats.");
    }
    
    /**
     * Helper method tạo Room
     */
    private Room createRoom(Cinema cinema, String roomNumber, Integer totalRows, Integer totalCols) {
        Room room = new Room();
        room.setCinema(cinema);
        room.setRoomNumber(roomNumber);
        room.setTotalRows(totalRows);
        room.setTotalCols(totalCols);
        room.setTotalSeats(totalRows * totalCols);
        return room;
    }
    
    /**
     * Tự động tạo ghế cho Room (giống logic trong RoomService)
     */
    private void createSeatsForRoom(Room room, Integer totalRows, Integer totalCols) {
        List<Seat> seats = new ArrayList<>();
        
        for (int row = 0; row < totalRows; row++) {
            char rowChar = (char) ('A' + row);
            
            // 2 hàng cuối là VIP
            SeatType seatType = (row >= totalRows - 2) ? SeatType.VIP : SeatType.NORMAL;
            
            for (int col = 1; col <= totalCols; col++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatNumber(String.valueOf(rowChar) + col);
                seat.setRow(String.valueOf(rowChar));
                seat.setCol(col);
                seat.setType(seatType);
                
                seats.add(seat);
            }
        }
        
        seatRepository.saveAll(seats);
    }
}

