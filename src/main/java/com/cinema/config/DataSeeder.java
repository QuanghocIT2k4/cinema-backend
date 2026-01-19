package com.cinema.config;

import com.cinema.model.entity.*;
import com.cinema.model.enums.BookingStatus;
import com.cinema.model.enums.MovieStatus;
import com.cinema.model.enums.SeatType;
import com.cinema.model.enums.UserRole;
import com.cinema.model.enums.UserStatus;
import com.cinema.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * DataSeeder: Tự động tạo dữ liệu mẫu khi ứng dụng khởi động
 * Chỉ chạy khi database rỗng để tránh duplicate
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final RefreshmentRepository refreshmentRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final BookingRefreshmentRepository bookingRefreshmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    @Override
    public void run(String... args) {
        log.info("=== BẮT ĐẦU SEED DATA ===");
        
        // Chỉ seed nếu database rỗng
        if (userRepository.count() > 0) {
            log.info("Database đã có dữ liệu, bỏ qua seed data");
            return;
        }
        
        // Seed từng phần riêng biệt với try-catch để nếu một phần fail thì các phần khác vẫn chạy được
        try {
        seedUsers();
        } catch (Exception e) {
            log.error("Lỗi khi seed Users: {}", e.getMessage(), e);
        }
        
        try {
        seedMovies();
        } catch (Exception e) {
            log.error("Lỗi khi seed Movies: {}", e.getMessage(), e);
        }
        
        try {
        seedCinemasAndRooms();
        } catch (Exception e) {
            log.error("Lỗi khi seed Cinemas và Rooms: {}", e.getMessage(), e);
        }
        
        try {
        seedRefreshments();
        } catch (Exception e) {
            log.error("Lỗi khi seed Refreshments: {}", e.getMessage(), e);
        }
        
        try {
        seedShowtimes();
        } catch (Exception e) {
            log.error("Lỗi khi seed Showtimes: {}", e.getMessage(), e);
        }
        
        try {
        seedBookings();
        } catch (Exception e) {
            log.error("Lỗi khi seed Bookings: {}", e.getMessage(), e);
        }
        
        log.info("=== HOÀN THÀNH SEED DATA ===");
        log.info("Tổng kết:");
        long adminCount = userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.ADMIN).count();
        long customerCount = userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.CUSTOMER).count();
        log.info("- Users: {} ({} Admin + {} Customer)", 
                userRepository.count(), adminCount, customerCount);
        log.info("- Movies: {}", movieRepository.count());
        log.info("- Cinemas: {}", cinemaRepository.count());
        log.info("- Rooms: {}", roomRepository.count());
        log.info("- Seats: {}", seatRepository.count());
        log.info("- Refreshments: {}", refreshmentRepository.count());
        log.info("- Showtimes: {}", showtimeRepository.count());
        log.info("- Bookings: {}", bookingRepository.count());
        log.info("- Tickets: {}", ticketRepository.count());
    }
    
    /**
     * Seed User: 3 Admin + 25 Customer (tổng 28 users)
     */
    private void seedUsers() {
        log.info("Đang seed Users...");
        
        List<User> users = new ArrayList<>();
        
        // Admin 1-3
        String[] adminNames = {"Admin Quản Trị", "Admin Phụ", "Admin Hệ Thống"};
        String[] adminEmails = {"admin@cinema.vn", "admin2@cinema.vn", "admin3@cinema.vn"};
        for (int i = 0; i < 3; i++) {
            User admin = new User();
            admin.setUsername("admin" + (i == 0 ? "" : (i + 1)));
            admin.setEmail(adminEmails[i]);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName(adminNames[i]);
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            users.add(admin);
        }
        
        // Customer 1-25 (random names)
        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi", "Đỗ", "Hồ"};
        String[] middleNames = {"Văn", "Thị", "Đức", "Minh", "Thanh", "Quang", "Hữu", "Công", "Đình", "Xuân"};
        String[] lastNames = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};
        
        for (int i = 1; i <= 25; i++) {
            User customer = new User();
            customer.setUsername("customer" + i);
            customer.setEmail("customer" + i + "@example.com");
            customer.setPassword(passwordEncoder.encode("123456"));
            
            // Random name
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String middleName = middleNames[random.nextInt(middleNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            customer.setFullName(firstName + " " + middleName + " " + lastName);
            
            // Random phone
            customer.setPhone("0" + (9 + random.nextInt(2)) + String.format("%08d", random.nextInt(100000000)));
            
            customer.setRole(UserRole.CUSTOMER);
            customer.setStatus(UserStatus.ACTIVE);
            users.add(customer);
        }
        
        userRepository.saveAll(users);
        log.info("Đã seed {} users ({} Admin + {} Customer)", 
                users.size(), 3, users.size() - 3);
    }
    
    /**
     * Seed Movie: Từ JSON file hoặc default (50-100 phim)
     */
    @Transactional
    private void seedMovies() {
        log.info("Đang seed Movies...");
        
        List<Movie> movies = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Thử load từ JSON file
        try {
            // Tìm file movies.json trong Example/movie-data/movies_data/
            Path jsonPath = Paths.get("..", "Example", "movie-data", "movies_data", "movies.json");
            File jsonFile = jsonPath.toFile();
            
            if (jsonFile.exists()) {
                log.info("Tìm thấy movies.json, đang load...");
                List<Map<String, Object>> moviesData = objectMapper.readValue(jsonFile, new TypeReference<List<Map<String, Object>>>() {});
                log.info("Đã load {} movies từ JSON", moviesData.size());
                
                // Lấy 80-100 phim đầu tiên
                int maxMovies = Math.min(100, moviesData.size());
                List<Map<String, Object>> selectedMovies = moviesData.subList(0, maxMovies);
                
                for (int i = 0; i < selectedMovies.size(); i++) {
                    Map<String, Object> movieData = selectedMovies.get(i);
                    try {
                        String name = (String) movieData.get("name");
                        String description = movieData.containsKey("description") ? (String) movieData.get("description") : "";
                        Integer duration = movieData.containsKey("duration") ? ((Number) movieData.get("duration")).intValue() : 120;
                        
                        // Lấy genre đầu tiên từ array
                        String genre = "Action";
                        if (movieData.containsKey("genres") && movieData.get("genres") instanceof List) {
                            List<?> genres = (List<?>) movieData.get("genres");
                            if (!genres.isEmpty()) {
                                genre = genres.get(0).toString();
                            }
                        }
                        
                        String poster = movieData.containsKey("poster_file") ? (String) movieData.get("poster_file") : "https://via.placeholder.com/500x750";
                        String trailer = movieData.containsKey("trailer_url") ? (String) movieData.get("trailer_url") : "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
                        String ageRating = movieData.containsKey("age_limit") ? "PG-" + movieData.get("age_limit") : "PG-13";
                        
                        // Phân bổ release date dựa trên index để có đủ 3 loại phim:
                        // 30% ENDED (phim cũ), 50% NOW_SHOWING (đang chiếu), 20% COMING_SOON (sắp chiếu)
                        MovieStatus status;
                        LocalDate releaseDate;
                        LocalDate endDate;
                        
                        double ratio = (double) i / selectedMovies.size();
                        if (ratio < 0.3) {
                            // 30% đầu: ENDED (phim đã kết thúc)
                            releaseDate = today.minusDays(60 + random.nextInt(30));
                            endDate = today.minusDays(random.nextInt(10));
                            status = MovieStatus.ENDED;
                        } else if (ratio < 0.8) {
                            // 50% giữa: NOW_SHOWING (đang chiếu)
                            releaseDate = today.minusDays(random.nextInt(30));
                            endDate = today.plusDays(30 + random.nextInt(30));
                            status = MovieStatus.NOW_SHOWING;
                        } else {
                            // 20% cuối: COMING_SOON (sắp chiếu)
                            releaseDate = today.plusDays(1 + random.nextInt(30));
                            endDate = releaseDate.plusDays(60);
                            status = MovieStatus.COMING_SOON;
                        }
                        
                        movies.add(createMovie(name, genre, duration, description, poster, trailer, releaseDate, endDate, status, ageRating));
                    } catch (Exception e) {
                        log.warn("Lỗi khi parse movie từ JSON: {}", e.getMessage());
                    }
                }
                
                log.info("Đã parse {} movies từ JSON", movies.size());
            } else {
                log.info("Không tìm thấy movies.json, dùng default seeding");
                seedMoviesDefault(movies, today);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi load JSON file: {}, dùng default seeding", e.getMessage());
            seedMoviesDefault(movies, today);
        }
        
        if (movies.isEmpty()) {
            seedMoviesDefault(movies, today);
        }
        
        movieRepository.saveAll(movies);
        
        long nowShowing = movies.stream().filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING).count();
        long comingSoon = movies.stream().filter(m -> m.getStatus() == MovieStatus.COMING_SOON).count();
        long ended = movies.stream().filter(m -> m.getStatus() == MovieStatus.ENDED).count();
        
        log.info("Đã seed {} movies ({} NOW_SHOWING + {} COMING_SOON + {} ENDED)",
                movies.size(), nowShowing, comingSoon, ended);
    }
    
    /**
     * Default movie seeding (nếu không có JSON file)
     */
    private void seedMoviesDefault(List<Movie> movies, LocalDate today) {
        String[] movieTitles = {
            "Avengers: Endgame", "Spider-Man: No Way Home", "The Matrix Resurrections",
            "Fast & Furious 10", "Dune", "Top Gun: Maverick", "No Time to Die",
            "The Conjuring 4", "Encanto", "Doraemon: Stand By Me 2",
            "Avatar 3", "Black Panther 2", "The Batman 2", "Doctor Strange 2",
            "Thor: Love and Thunder", "Black Widow", "Shang-Chi", "Eternals"
        };
        
        String[] genres = {"Action", "Animation", "Horror", "Sci-Fi", "Comedy", "Drama"};
        
        for (int i = 0; i < Math.min(50, movieTitles.length * 3); i++) {
            String title = movieTitles[i % movieTitles.length] + (i >= movieTitles.length ? " " + (i / movieTitles.length + 1) : "");
            String genre = genres[random.nextInt(genres.length)];
            int duration = 90 + random.nextInt(90); // 90-180 phút
            LocalDate releaseDate = today.minusDays(random.nextInt(60)).plusDays(random.nextInt(30));
            
            MovieStatus status;
            LocalDate endDate;
            if (releaseDate.isBefore(today.minusDays(30))) {
                status = MovieStatus.ENDED;
                endDate = releaseDate.plusDays(60);
            } else if (releaseDate.isAfter(today)) {
                status = MovieStatus.COMING_SOON;
                endDate = releaseDate.plusDays(60);
            } else {
                status = MovieStatus.NOW_SHOWING;
                endDate = releaseDate.plusDays(60);
            }
            
            movies.add(createMovie(title, genre, duration, 
                    "Mô tả phim " + title, 
                    "https://via.placeholder.com/500x750",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    releaseDate, endDate, status, "PG-13"));
        }
    }
    
    /**
     * Helper method tạo Movie
     */
    private Movie createMovie(String title, String genre, Integer duration,
                              String description, String poster, String trailer,
                              LocalDate releaseDate, LocalDate endDate,
                              MovieStatus status, String ageRating) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setDuration(duration);
        movie.setDescription(description);
        movie.setPoster(poster);
        movie.setTrailer(trailer);
        movie.setReleaseDate(releaseDate);
        movie.setEndDate(endDate);
        movie.setStatus(status);
        movie.setAgeRating(ageRating);
        return movie;
    }
    
    /**
     * Seed Cinema và Room (tự động tạo ghế)
     */
    @Transactional
    private void seedCinemasAndRooms() {
        log.info("Đang seed Cinemas và Rooms...");
        
        // Cinema 1: CGV Vincom
        Cinema cinema1 = new Cinema();
        cinema1.setName("CGV Vincom");
        cinema1.setAddress("123 Đường Lê Lợi, Quận 1, TP.HCM");
        cinema1.setPhone("0123456789");
        cinema1.setEmail("cgv@example.com");
        cinema1 = cinemaRepository.save(cinema1);
        
        // Rooms cho Cinema 1
        createRoom(cinema1, "Phòng 1", 10, 10, SeatType.NORMAL);
        createRoom(cinema1, "Phòng 2", 12, 12, SeatType.NORMAL);
        createRoom(cinema1, "Phòng 3", 8, 8, SeatType.NORMAL);
        createRoom(cinema1, "IMAX 1", 15, 20, SeatType.VIP);
        
        // Cinema 2: Lotte Cinema
        Cinema cinema2 = new Cinema();
        cinema2.setName("Lotte Cinema");
        cinema2.setAddress("456 Đường Nguyễn Huệ, Quận 1, TP.HCM");
        cinema2.setPhone("0987654321");
        cinema2.setEmail("lotte@example.com");
        cinema2 = cinemaRepository.save(cinema2);
        
        // Rooms cho Cinema 2
        createRoom(cinema2, "Phòng 1", 10, 10, SeatType.NORMAL);
        createRoom(cinema2, "Phòng 2", 10, 10, SeatType.NORMAL);
        createRoom(cinema2, "Phòng 3", 12, 12, SeatType.NORMAL);
        
        // Cinema 3: Galaxy Cinema
        Cinema cinema3 = new Cinema();
        cinema3.setName("Galaxy Cinema");
        cinema3.setAddress("789 Đường Điện Biên Phủ, Quận Bình Thạnh, TP.HCM");
        cinema3.setPhone("0912345678");
        cinema3.setEmail("galaxy@example.com");
        cinema3 = cinemaRepository.save(cinema3);
        
        // Rooms cho Cinema 3
        createRoom(cinema3, "Phòng 1", 10, 10, SeatType.NORMAL);
        createRoom(cinema3, "Phòng 2", 10, 10, SeatType.NORMAL);
        createRoom(cinema3, "Phòng 3", 10, 10, SeatType.NORMAL);
        
        log.info("Đã seed 3 cinemas và 10 rooms (tự động tạo ghế)");
    }

    /**
     * Seed Showtime: 20-30 suất chiếu mẫu, phân bổ theo nhiều phòng và nhiều phim,
     * đảm bảo không xung đột thời gian trong cùng phòng.
     */
    @Transactional
    private void seedShowtimes() {
        log.info("Đang seed Showtimes...");

        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            log.warn("Không có room nào, bỏ qua seed showtimes");
            return;
        }

        List<Movie> nowShowingMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING)
                .toList();

        if (nowShowingMovies.isEmpty()) {
            log.warn("Không có movie NOW_SHOWING, bỏ qua seed showtimes");
            return;
        }

        // Mục tiêu 30 suất chiếu (đúng roadmap 20-30)
        int target = 30;
        List<Showtime> showtimes = new ArrayList<>(target);

        LocalDate today = LocalDate.now();
        List<LocalTime> baseSlots = List.of(
                LocalTime.of(9, 0),
                LocalTime.of(11, 30),
                LocalTime.of(14, 0),
                LocalTime.of(16, 30),
                LocalTime.of(19, 0),
                LocalTime.of(21, 30)
        );

        int daySpan = 5; // rải trong 5 ngày để dễ hợp lý

        outer:
        for (int d = 0; d < daySpan; d++) {
            LocalDate date = today.plusDays(d);

            // Shuffle để phân bổ đa dạng
            List<Room> shuffledRooms = new ArrayList<>(rooms);
            Collections.shuffle(shuffledRooms, random);

            for (Room room : shuffledRooms) {
                // Mỗi phòng 1-2 suất/ngày
                int perRoomPerDay = 1 + random.nextInt(2);
                List<LocalTime> slots = new ArrayList<>(baseSlots);
                Collections.shuffle(slots, random);

                for (int i = 0; i < Math.min(perRoomPerDay, slots.size()); i++) {
                    if (showtimes.size() >= target) break outer;

                    Movie movie = nowShowingMovies.get(random.nextInt(nowShowingMovies.size()));
                    int durationMinutes = movie.getDuration() != null ? movie.getDuration() : (100 + random.nextInt(60));

                    LocalTime slot = slots.get(i);
                    // random +/- 0-10 phút để nhìn tự nhiên
                    int offset = random.nextBoolean() ? random.nextInt(11) : -random.nextInt(11);
                    LocalDateTime start = date.atTime(slot).plusMinutes(offset);
                    // thêm 10 phút buffer dọn phòng
                    LocalDateTime end = start.plusMinutes(durationMinutes + 10L);

                    // Không để quá trễ (sau 23:59)
                    if (end.toLocalTime().isAfter(LocalTime.of(23, 59))) {
                        continue;
                    }

                    // Check conflict trong room (dùng query sẵn có)
                    boolean hasConflict = !showtimeRepository.findConflictingShowtimes(room.getId(), start, end).isEmpty();
                    if (hasConflict) {
                        continue;
                    }

                    Showtime showtime = new Showtime();
                    showtime.setMovie(movie);
                    showtime.setRoom(room);
                    showtime.setStartTime(start);
                    showtime.setEndTime(end);

                    // Giá vé: 65k - 150k (IMAX/VIP rooms thường đắt hơn)
                    BigDecimal basePrice = BigDecimal.valueOf(65000 + random.nextInt(85001));
                    if (room.getRoomNumber() != null && room.getRoomNumber().toUpperCase().contains("IMAX")) {
                        basePrice = basePrice.add(BigDecimal.valueOf(50000));
                    }
                    showtime.setPrice(basePrice);

                    showtimes.add(showtime);
                }
            }
        }

        if (showtimes.isEmpty()) {
            log.warn("Không seed được showtime nào (có thể do conflict), bỏ qua");
            return;
        }

        showtimeRepository.saveAll(showtimes);
        log.info("Đã seed {} showtimes", showtimes.size());
    }
    
    /**
     * Helper method tạo Room và tự động tạo ghế
     */
    private void createRoom(Cinema cinema, String roomNumber, int rows, int cols, SeatType defaultSeatType) {
        Room room = new Room();
        room.setCinema(cinema);
        room.setRoomNumber(roomNumber);
        room.setTotalRows(rows);
        room.setTotalCols(cols);
        room.setTotalSeats(rows * cols);
        room = roomRepository.save(room);
        
        // Tự động tạo ghế
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            String rowLetter = getRowLetter(row);
            for (int col = 1; col <= cols; col++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatNumber(rowLetter + col);
                seat.setRow(rowLetter);
                seat.setCol(col);
                // Hàng cuối cùng là VIP (nếu defaultSeatType là VIP)
                if (defaultSeatType == SeatType.VIP || row == rows - 1) {
                    seat.setType(SeatType.VIP);
                } else {
                    seat.setType(SeatType.NORMAL);
                }
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }
    
    /**
     * Chuyển số hàng thành chữ cái: 0->A, 1->B, ..., 25->Z, 26->AA, ...
     */
    private String getRowLetter(int rowIndex) {
        if (rowIndex < 26) {
            return String.valueOf((char) ('A' + rowIndex));
        } else {
            int firstLetter = (rowIndex / 26) - 1;
            int secondLetter = rowIndex % 26;
            return String.valueOf((char) ('A' + firstLetter)) + (char) ('A' + secondLetter);
        }
    }
    
    /**
     * Seed Refreshment: 10 đồ ăn/nước uống
     */
    @Transactional
    private void seedRefreshments() {
        log.info("Đang seed Refreshments...");
        
        List<Refreshment> refreshments = new ArrayList<>();
        
        // Bắp rang
        refreshments.add(createRefreshment("Bắp rang bơ (Size S)", "https://via.placeholder.com/300x300", 30000));
        refreshments.add(createRefreshment("Bắp rang bơ (Size M)", "https://via.placeholder.com/300x300", 45000));
        refreshments.add(createRefreshment("Bắp rang bơ (Size L)", "https://via.placeholder.com/300x300", 60000));
        
        // Nước ngọt
        refreshments.add(createRefreshment("Coca Cola (Size M)", "https://via.placeholder.com/300x300", 25000));
        refreshments.add(createRefreshment("Pepsi (Size M)", "https://via.placeholder.com/300x300", 25000));
        refreshments.add(createRefreshment("7Up (Size M)", "https://via.placeholder.com/300x300", 25000));
        
        // Combo
        refreshments.add(createRefreshment("Combo 1 (Bắp M + Nước M)", "https://via.placeholder.com/300x300", 65000));
        refreshments.add(createRefreshment("Combo 2 (Bắp L + 2 Nước M)", "https://via.placeholder.com/300x300", 95000));
        
        // Snack khác
        refreshments.add(createRefreshment("Snack khoai tây", "https://via.placeholder.com/300x300", 20000));
        refreshments.add(createRefreshment("Nước suối", "https://via.placeholder.com/300x300", 15000));
        
        refreshmentRepository.saveAll(refreshments);
        log.info("Đã seed {} refreshments", refreshments.size());
    }
    
    /**
     * Helper method tạo Refreshment
     */
    private Refreshment createRefreshment(String name, String picture, double price) {
        Refreshment refreshment = new Refreshment();
        refreshment.setName(name);
        refreshment.setPicture(picture);
        refreshment.setPrice(BigDecimal.valueOf(price));
        refreshment.setIsCurrent(true);
        return refreshment;
    }
    
    /**
     * Seed Booking: 18 bookings (PAID, PENDING, CANCELLED)
     */
    @Transactional
    private void seedBookings() {
        log.info("Đang seed Bookings...");
        
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .toList();
        
        List<Showtime> showtimes = showtimeRepository.findAll();
        List<Refreshment> refreshments = refreshmentRepository.findAll();
        
        if (customers.isEmpty() || showtimes.isEmpty()) {
            log.warn("Không có customer hoặc showtime, bỏ qua seed bookings");
            return;
        }
        
        List<Booking> bookings = new ArrayList<>();
        int targetBookings = 18;
        
        for (int i = 0; i < targetBookings && i < showtimes.size(); i++) {
            Showtime showtime = showtimes.get(i % showtimes.size());
            User customer = customers.get(random.nextInt(customers.size()));
            
            // Tạo booking
            Booking booking = new Booking();
            booking.setUser(customer);
            booking.setShowtime(showtime);
            booking.setBookingCode(generateBookingCode());
            
            // Phân bổ status: 60% PAID, 20% PENDING, 20% CANCELLED
            double ratio = (double) i / targetBookings;
            BookingStatus status;
            if (ratio < 0.6) {
                status = BookingStatus.PAID;
                booking.setPaymentTime(LocalDateTime.now().minusDays(random.nextInt(30)));
            } else if (ratio < 0.8) {
                status = BookingStatus.PENDING;
            } else {
                status = BookingStatus.CANCELLED;
            }
            booking.setStatus(status);
            // Tạm thời set totalPrice = 0 để tránh lỗi NOT NULL khi insert lần đầu
            booking.setTotalPrice(BigDecimal.ZERO);
            
            // Lưu booking trước để có ID
            booking = bookingRepository.save(booking);
            
            // Tạo tickets (1-4 ghế ngẫu nhiên)
            int numSeats = 1 + random.nextInt(4);
            List<Seat> availableSeats = seatRepository.findByRoomId(showtime.getRoom().getId());
            Collections.shuffle(availableSeats, random);
            
            BigDecimal ticketTotal = BigDecimal.ZERO;
            List<Ticket> tickets = new ArrayList<>();
            
            for (int j = 0; j < Math.min(numSeats, availableSeats.size()); j++) {
                Seat seat = availableSeats.get(j);
                Ticket ticket = new Ticket();
                ticket.setBooking(booking);
                ticket.setSeat(seat);
                ticket.setPrice(showtime.getPrice());
                tickets.add(ticket);
                ticketTotal = ticketTotal.add(showtime.getPrice());
            }
            
            ticketRepository.saveAll(tickets);
            
            // Thêm refreshments ngẫu nhiên (50% có mua đồ ăn)
            BigDecimal refreshmentTotal = BigDecimal.ZERO;
            if (!refreshments.isEmpty() && random.nextBoolean()) {
                int numRefreshments = 1 + random.nextInt(3);
                // Dùng Set để tránh duplicate refreshment trong cùng booking
                Set<Refreshment> selectedRefreshments = new HashSet<>();
                List<Refreshment> availableRefreshments = new ArrayList<>(refreshments);
                Collections.shuffle(availableRefreshments, random);
                
                for (int k = 0; k < numRefreshments && k < availableRefreshments.size(); k++) {
                    Refreshment refreshment = availableRefreshments.get(k);
                    // Tránh duplicate: chỉ thêm nếu chưa có trong Set
                    if (selectedRefreshments.add(refreshment)) {
                        int quantity = 1 + random.nextInt(2);
                        
                        BookingRefreshment br = new BookingRefreshment();
                        br.setBooking(booking);
                        br.setRefreshment(refreshment);
                        br.setQuantity(quantity);
                        // Tổng tiền cho combo này = đơn giá * số lượng
                        BigDecimal itemTotal = refreshment.getPrice().multiply(BigDecimal.valueOf(quantity));
                        br.setTotalPrice(itemTotal);
                        bookingRefreshmentRepository.save(br);
                        
                        refreshmentTotal = refreshmentTotal.add(itemTotal);
                    }
                }
            }
            
            // Cập nhật tổng tiền
            booking.setTotalPrice(ticketTotal.add(refreshmentTotal));
            bookingRepository.save(booking);
            
            bookings.add(booking);
        }
        
        long paidCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PAID).count();
        long pendingCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long cancelledCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        
        log.info("Đã seed {} bookings ({} PAID + {} PENDING + {} CANCELLED)",
                bookings.size(), paidCount, pendingCount, cancelledCount);
    }
    
    /**
     * Tạo mã booking ngẫu nhiên (format: BK + timestamp + random)
     */
    private String generateBookingCode() {
        long timestamp = System.currentTimeMillis() % 1000000;
        int randomNum = random.nextInt(1000);
        return String.format("BK%06d%03d", timestamp, randomNum);
    }
}

