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
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
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
    private final MovieActorRepository movieActorRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final RefreshmentRepository refreshmentRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final BookingRefreshmentRepository bookingRefreshmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    @Override
    public void run(String... args) {
        log.info("=== BẮT ĐẦU SEED DATA ===");

        // Trường hợp 1: DB đã có users/movies nhưng có thể thiếu một số data
        // → seed các phần còn thiếu
        if (userRepository.count() > 0) {
            log.info("Database đã có dữ liệu. Kiểm tra và seed các phần còn thiếu...");
            
            // Seed movie_actors nếu chưa có hoặc thiếu avatar_url
            if (movieActorRepository.count() == 0 && movieRepository.count() > 0) {
                log.info("Seed bảng movie_actors từ cast của movie...");
                try {
                    seedMovieActorsFromExistingMovies();
                } catch (Exception e) {
                    log.error("Lỗi khi seed MovieActors từ dữ liệu hiện có: {}", e.getMessage(), e);
                }
            } else {
                // Update avatar_url nếu thiếu
                boolean hasDataWithoutAvatar = movieActorRepository.findAll().stream()
                        .anyMatch(ma -> ma.getAvatarUrl() == null);
                if (hasDataWithoutAvatar) {
                    log.info("Phát hiện movie_actors thiếu avatar_url, sẽ update...");
                    try {
                        seedMovieActorsFromExistingMovies();
                    } catch (Exception e) {
                        log.error("Lỗi khi update MovieActors avatar_url: {}", e.getMessage(), e);
                    }
                }
            }
            
            // Seed showtimes: nếu quá ít thì xóa và seed lại
            long showtimeCount = showtimeRepository.count();
            long expectedMinShowtimes = movieRepository.count() * 8 * 7; // Mỗi phim 8 suất/ngày × 7 ngày tối thiểu
            
            if (showtimeCount == 0 || showtimeCount < expectedMinShowtimes) {
                log.info("Showtimes hiện có {} suất (mong đợi tối thiểu {}), sẽ xóa và seed lại...", 
                        showtimeCount, expectedMinShowtimes);
                try {
                    // Xóa showtimes cũ (và các bảng liên quan nếu cần)
                    if (showtimeCount > 0) {
                        log.info("Đang xóa {} showtimes cũ...", showtimeCount);
                        // Xóa các bảng liên quan theo đúng thứ tự FK
                        bookingRefreshmentRepository.deleteAll();
                        ticketRepository.deleteAll();
                        bookingRepository.deleteAll();
                        // Sau đó xóa showtimes
                        showtimeRepository.deleteAll();
                        log.info("Đã xóa showtimes cũ thành công");
                    }
                    // Seed lại showtimes
                    seedShowtimes();
                } catch (Exception e) {
                    log.error("Lỗi khi seed Showtimes: {}", e.getMessage(), e);
                }
            } else {
                log.info("Showtimes đã có đủ ({} suất chiếu), bỏ qua seed.", showtimeCount);
            }
            
            // Seed reviews nếu chưa có
            long reviewCount = reviewRepository.count();
            if (reviewCount == 0 && movieRepository.count() > 0) {
                log.info("Seed reviews (hiện có {} reviews)...", reviewCount);
                try {
                    seedReviews();
                } catch (Exception e) {
                    log.error("Lỗi khi seed Reviews: {}", e.getMessage(), e);
                }
            } else {
                log.info("Reviews đã có ({}), bỏ qua seed.", reviewCount);
            }

            return;
        }

        // Trường hợp 2: Database rỗng → seed full data như cũ
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

        // Sau khi seed movies xong cho DB rỗng → sinh movie_actors từ cast
        try {
            seedMovieActorsFromExistingMovies();
        } catch (Exception e) {
            log.error("Lỗi khi seed MovieActors sau khi seed Movies: {}", e.getMessage(), e);
        }

        // Seed reviews cho mỗi movie
        try {
            seedReviews();
        } catch (Exception e) {
            log.error("Lỗi khi seed Reviews: {}", e.getMessage(), e);
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
            admin.setUsername("admin" + (i == 0 ? "" : String.valueOf(i + 1)));
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

                        // Đọc director từ JSON
                        String director = "Unknown Director";
                        if (movieData.containsKey("directors") && movieData.get("directors") instanceof List) {
                            List<?> directors = (List<?>) movieData.get("directors");
                            if (!directors.isEmpty()) {
                                director = directors.get(0).toString();
                            }
                        } else if (movieData.containsKey("director")) {
                            director = movieData.get("director").toString();
                        } else {
                            director = getDefaultDirector(name);
                        }

                        // Đọc actors từ JSON (field "actors" là array)
                        String cast = "";
                        if (movieData.containsKey("actors") && movieData.get("actors") instanceof List) {
                            List<?> actors = (List<?>) movieData.get("actors");
                            List<String> actorNames = new ArrayList<>();
                            for (Object actorObj : actors) {
                                if (actorObj instanceof Map) {
                                    Map<?, ?> actorMap = (Map<?, ?>) actorObj;
                                    if (actorMap.containsKey("name")) {
                                        actorNames.add(actorMap.get("name").toString());
                                    }
                                }
                            }
                            if (!actorNames.isEmpty()) {
                                cast = String.join(", ", actorNames);
                            }
                        }
                        
                        // Fallback nếu không có actors trong JSON
                        if (cast.isEmpty()) {
                            cast = getDefaultCast(name);
                        }
                        
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

                        movies.add(createMovie(
                                name, genre, duration, description,
                                poster, trailer, releaseDate, endDate,
                                status, ageRating, director, cast
                        ));
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
            
            String description = "Mô tả phim " + title;
            String poster = "https://via.placeholder.com/500x750";
            String trailer = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

            String director = getDefaultDirector(title);
            String cast = getDefaultCast(title);

            movies.add(createMovie(
                    title, genre, duration,
                    description,
                    poster,
                    trailer,
                    releaseDate, endDate, status, "PG-13",
                    director, cast
            ));
        }
    }
    
    /**
     * Helper method tạo Movie
     */
    private Movie createMovie(String title, String genre, Integer duration,
                              String description, String poster, String trailer,
                              LocalDate releaseDate, LocalDate endDate,
                              MovieStatus status, String ageRating,
                              String director, String cast) {
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
        movie.setDirector(director);
        movie.setCast(cast);
        return movie;
    }

    /**
     * Map title -> director
     */
    private String getDefaultDirector(String title) {
        if (title == null) return "Unknown Director";
        String t = title.toLowerCase();

        if (t.contains("avengers")) return "Anthony & Joe Russo";
        if (t.contains("spider-man") || t.contains("spider man")) return "Jon Watts";
        if (t.contains("matrix")) return "Lana Wachowski";
        if (t.contains("fast & furious") || t.contains("fast and furious") || t.contains("fast & furious 10"))
            return "Justin Lin";
        if (t.contains("dune")) return "Denis Villeneuve";
        if (t.contains("top gun")) return "Joseph Kosinski";
        if (t.contains("no time to die")) return "Cary Joji Fukunaga";
        if (t.contains("conjuring")) return "James Wan";
        if (t.contains("encanto")) return "Jared Bush & Byron Howard";
        if (t.contains("avatar")) return "James Cameron";
        if (t.contains("black panther")) return "Ryan Coogler";
        if (t.contains("batman")) return "Matt Reeves";
        if (t.contains("doctor strange")) return "Sam Raimi";
        if (t.contains("thor")) return "Taika Waititi";
        if (t.contains("black widow")) return "Cate Shortland";
        if (t.contains("shang-chi") || t.contains("shang chi")) return "Destin Daniel Cretton";
        if (t.contains("eternals")) return "Chloé Zhao";
        if (t.contains("doraemon")) return "Ryuichi Yagi & Takashi Yamazaki";

        return "Unknown Director";
    }

    /**
     * Map title -> cast (main actors)
     */
    private String getDefaultCast(String title) {
        if (title == null) return "Unknown Cast";
        String t = title.toLowerCase();

        if (t.contains("avengers")) return "Robert Downey Jr., Chris Evans, Chris Hemsworth, Scarlett Johansson";
        if (t.contains("spider-man") || t.contains("spider man"))
            return "Tom Holland, Zendaya, Benedict Cumberbatch";
        if (t.contains("matrix")) return "Keanu Reeves, Carrie-Anne Moss";
        if (t.contains("fast & furious") || t.contains("fast and furious") || t.contains("fast & furious 10"))
            return "Vin Diesel, Michelle Rodriguez, Tyrese Gibson";
        if (t.contains("dune")) return "Timothée Chalamet, Zendaya, Oscar Isaac";
        if (t.contains("top gun")) return "Tom Cruise, Miles Teller";
        if (t.contains("no time to die")) return "Daniel Craig, Léa Seydoux, Rami Malek";
        if (t.contains("conjuring")) return "Vera Farmiga, Patrick Wilson";
        if (t.contains("encanto")) return "Stephanie Beatriz, María Cecilia Botero";
        if (t.contains("avatar")) return "Sam Worthington, Zoe Saldana, Sigourney Weaver";
        if (t.contains("black panther")) return "Chadwick Boseman, Michael B. Jordan, Lupita Nyong'o";
        if (t.contains("batman")) return "Robert Pattinson, Zoë Kravitz";
        if (t.contains("doctor strange")) return "Benedict Cumberbatch, Elizabeth Olsen";
        if (t.contains("thor")) return "Chris Hemsworth, Natalie Portman, Tessa Thompson";
        if (t.contains("black widow")) return "Scarlett Johansson, Florence Pugh";
        if (t.contains("shang-chi") || t.contains("shang chi"))
            return "Simu Liu, Awkwafina, Tony Leung";
        if (t.contains("eternals")) return "Gemma Chan, Richard Madden, Angelina Jolie";
        if (t.contains("doraemon")) return "Wasabi Mizuta, Megumi Ohara";

        return "Unknown Cast";
    }

    /**
     * Sinh dữ liệu movie_actors từ field cast của từng movie.
     * - Split theo dấu phẩy
     * - Trim khoảng trắng
     * - Tìm ảnh actor từ folder cinema-data/actor_photos và match theo tên
     * - Lưu URL ảnh vào avatarUrl
     * - Nếu đã có data nhưng avatar_url = null, sẽ update lại
     */
    @Transactional
    public void seedMovieActorsFromExistingMovies() {
        log.info("Đang seed MovieActors từ field cast của Movie...");

        // Kiểm tra xem có cần update avatar_url cho data cũ không
        boolean hasDataWithoutAvatar = movieActorRepository.count() > 0 && 
                movieActorRepository.findAll().stream().anyMatch(ma -> ma.getAvatarUrl() == null);
        
        if (movieActorRepository.count() > 0 && !hasDataWithoutAvatar) {
            log.info("Bảng movie_actors đã có dữ liệu và đã có avatar_url ({} bản ghi), bỏ qua seed.", 
                    movieActorRepository.count());
            return;
        }
        
        if (hasDataWithoutAvatar) {
            log.info("Phát hiện data cũ chưa có avatar_url, sẽ update lại...");
        }

        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            log.warn("Không có movie nào, bỏ qua seed MovieActors.");
            return;
        }

        // Đọc danh sách file ảnh từ folder cinema-data/actor_photos
        // Thử nhiều đường dẫn: relative từ project root, hoặc từ parent directory
        Path actorPhotosPath = null;
        Path[] possiblePaths = {
            Paths.get("cinema-data", "actor_photos"),  // Từ project root
            Paths.get("..", "cinema-data", "actor_photos"),  // Từ cinema-backend folder
            Paths.get(System.getProperty("user.dir"), "cinema-data", "actor_photos"),  // Absolute từ working dir
            Paths.get(System.getProperty("user.dir"), "..", "cinema-data", "actor_photos")  // Absolute từ parent
        };
        
        for (Path path : possiblePaths) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                actorPhotosPath = path;
                log.info("Tìm thấy folder actor_photos tại: {}", path.toAbsolutePath());
                break;
            }
        }
        
        Map<String, String> actorPhotoMap = new HashMap<>();
        
        if (actorPhotosPath != null) {
            try {
                Files.list(actorPhotosPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".jpg"))
                    .forEach(photoPath -> {
                        String fileName = photoPath.getFileName().toString().toLowerCase();
                        // Lưu mapping: tên file (lowercase) -> đường dẫn file
                        actorPhotoMap.put(fileName, photoPath.toString());
                    });
                log.info("Đã load {} file ảnh actor từ folder local", actorPhotoMap.size());
            } catch (IOException e) {
                log.warn("Không thể đọc folder actor_photos: {}", e.getMessage());
            }
        } else {
            log.warn("Folder actor_photos không tìm thấy. Đã thử các đường dẫn:");
            for (Path path : possiblePaths) {
                log.warn("  - {}", path.toAbsolutePath());
            }
        }

        List<MovieActor> movieActors = new ArrayList<>();
        List<MovieActor> existingActors = movieActorRepository.findAll();
        Map<String, MovieActor> existingMap = new HashMap<>();
        
        // Tạo map các actor đã tồn tại: key = movieId_actorName
        for (MovieActor existing : existingActors) {
            String key = existing.getMovie().getId() + "_" + existing.getName().toLowerCase();
            existingMap.put(key, existing);
        }
        
        Set<String> processedActors = new HashSet<>(); // Để tránh duplicate

        for (Movie movie : movies) {
            String cast = movie.getCast();
            if (cast == null || cast.trim().isEmpty() || "Unknown Cast".equalsIgnoreCase(cast.trim())) {
                continue;
            }

            String[] names = cast.split(",");
            for (String rawName : names) {
                String name = rawName.trim();
                if (name.isEmpty()) continue;

                // Tạo key để check duplicate (movie_id + actor_name)
                String uniqueKey = movie.getId() + "_" + name.toLowerCase();
                if (processedActors.contains(uniqueKey)) {
                    continue; // Đã xử lý actor này cho movie này rồi
                }
                processedActors.add(uniqueKey);

                MovieActor ma;
                // Nếu đã có actor này rồi, lấy ra để update avatar_url
                if (existingMap.containsKey(uniqueKey)) {
                    ma = existingMap.get(uniqueKey);
                } else {
                    // Tạo mới
                    ma = new MovieActor();
                    ma.setMovie(movie);
                    ma.setName(name);
                }
                
                // Tìm ảnh actor từ folder local (có thể match theo movie_id hoặc tên actor)
                String avatarUrl = findActorPhoto(name, movie.getId(), actorPhotoMap);
                
                // Chỉ update nếu chưa có avatar_url hoặc tìm thấy ảnh mới
                if (ma.getAvatarUrl() == null || avatarUrl != null) {
                    ma.setAvatarUrl(avatarUrl);
                }
                
                if (avatarUrl != null) {
                    log.debug("Đã tìm thấy ảnh cho actor: {} (movie: {}) -> {}", name, movie.getId(), avatarUrl);
                } else {
                    log.debug("Không tìm thấy ảnh cho actor: {} (movie: {})", name, movie.getId());
                }
                
                movieActors.add(ma);
            }
        }

        if (movieActors.isEmpty()) {
            log.warn("Không tìm được diễn viên nào từ field cast, không insert bản ghi MovieActor.");
            return;
        }

        movieActorRepository.saveAll(movieActors);
        long withPhoto = movieActors.stream().filter(ma -> ma.getAvatarUrl() != null).count();
        log.info("Đã seed/update {} bản ghi movie_actors từ dữ liệu cast của movies ({} có ảnh).", 
                movieActors.size(), withPhoto);
    }
    
    /**
     * Tìm ảnh actor từ folder local dựa trên tên actor.
     * Format file: {movie_id_from_json}_{Actor_Name}.jpg
     * 
     * Logic match giống Example: fileName.toLowerCase().includes(actorName.toLowerCase().replace(/\s+/g, '_'))
     */
    private String findActorPhoto(String actorName, Long movieId, Map<String, String> actorPhotoMap) {
        if (actorName == null || actorName.trim().isEmpty() || actorPhotoMap.isEmpty()) {
            return null;
        }
        
        // Chuẩn hóa tên actor: lowercase, thay space bằng underscore (giống Example)
        String normalizedName = actorName.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");
        
        // Tìm file match: fileName.toLowerCase().includes(normalizedName)
        for (Map.Entry<String, String> entry : actorPhotoMap.entrySet()) {
            String fileName = entry.getKey(); // Đã là lowercase rồi
            
            // Check nếu tên file chứa tên actor đã normalized (giống Example)
            if (fileName.contains(normalizedName)) {
                Path photoPath = Paths.get(entry.getValue());
                String originalFileName = photoPath.getFileName().toString();
                return "/api/images/actors/" + originalFileName;
            }
        }
        
        // Nếu không tìm thấy exact match, thử match theo các phần của tên
        String[] nameParts = normalizedName.split("_");
        for (String part : nameParts) {
            if (part.length() < 3) continue; // Bỏ qua phần quá ngắn
            
            for (Map.Entry<String, String> entry : actorPhotoMap.entrySet()) {
                String fileName = entry.getKey();
                if (fileName.contains(part)) {
                    Path photoPath = Paths.get(entry.getValue());
                    String originalFileName = photoPath.getFileName().toString();
                    return "/api/images/actors/" + originalFileName;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Seed Cinema và Room (tự động tạo ghế)
     */
    @Transactional
    private void seedCinemasAndRooms() {
        log.info("Đang seed Cinemas và Rooms (đa chi nhánh)...");

        List<Cinema> cinemas = new ArrayList<>();

        // Một số rạp lớn tại TP.HCM / Hà Nội / Đà Nẵng / Cần Thơ...
        cinemas.add(createCinema("CGV Vincom Đồng Khởi",
                "123 Đường Lê Lợi, Quận 1, TP.HCM", "0123456789", "cgv-vincom@example.com"));
        cinemas.add(createCinema("Lotte Cinema Nguyễn Huệ",
                "456 Đường Nguyễn Huệ, Quận 1, TP.HCM", "0987654321", "lotte-nguyenhue@example.com"));
        cinemas.add(createCinema("Galaxy Cinema Điện Biên Phủ",
                "789 Đường Điện Biên Phủ, Bình Thạnh, TP.HCM", "0912345678", "galaxy-dbp@example.com"));
        cinemas.add(createCinema("BHD Star Bitexco",
                "2 Hải Triều, Quận 1, TP.HCM", "0909000111", "bhd-bitexco@example.com"));
        cinemas.add(createCinema("Cinestar Hai Bà Trưng",
                "135 Hai Bà Trưng, Quận 3, TP.HCM", "0909000222", "cinestar-hbt@example.com"));
        cinemas.add(createCinema("CGV Aeon Mall Bình Tân",
                "1 Đường Số 17A, Bình Trị Đông B, Bình Tân, TP.HCM", "0909000333", "cgv-aeonbt@example.com"));
        cinemas.add(createCinema("CGV Vincom Bà Triệu",
                "191 Bà Triệu, Hai Bà Trưng, Hà Nội", "02436668888", "cgv-batrieu@example.com"));
        cinemas.add(createCinema("Lotte Cinema Tây Sơn",
                "54A Tây Sơn, Đống Đa, Hà Nội", "02439996666", "lotte-tayson@example.com"));
        cinemas.add(createCinema("CGV Vincom Đà Nẵng",
                "910A Ngô Quyền, Sơn Trà, Đà Nẵng", "02363555555", "cgv-danang@example.com"));
        cinemas.add(createCinema("Beta Cineplex Cầu Giấy",
                "238 Hoàng Quốc Việt, Cầu Giấy, Hà Nội", "02432223333", "beta-caugiay@example.com"));
        cinemas.add(createCinema("Galaxy Cần Thơ",
                "1 Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ", "02923737373", "galaxy-cantho@example.com"));
        cinemas.add(createCinema("Cinestar Đà Lạt",
                "40 Trần Phú, Đà Lạt, Lâm Đồng", "02633669988", "cinestar-dalat@example.com"));

        int totalRooms = 0;

        for (int i = 0; i < cinemas.size(); i++) {
            Cinema cinema = cinemaRepository.save(cinemas.get(i));

            // Tuỳ theo index mà tạo cấu hình phòng khác nhau cho đa dạng
            if (i % 3 == 0) {
                createRoom(cinema, "Phòng 1", 10, 10, SeatType.NORMAL);
                createRoom(cinema, "Phòng 2", 12, 12, SeatType.NORMAL);
                createRoom(cinema, "IMAX 1", 15, 20, SeatType.VIP);
                totalRooms += 3;
            } else if (i % 3 == 1) {
                createRoom(cinema, "Phòng 1", 8, 12, SeatType.NORMAL);
                createRoom(cinema, "Phòng 2", 10, 14, SeatType.NORMAL);
                totalRooms += 2;
            } else {
                createRoom(cinema, "Phòng 1", 9, 10, SeatType.NORMAL);
                createRoom(cinema, "Phòng 2", 9, 10, SeatType.NORMAL);
                createRoom(cinema, "Phòng 3", 9, 10, SeatType.NORMAL);
                totalRooms += 3;
            }
        }

        log.info("Đã seed {} cinemas và {} rooms (tự động tạo ghế)", cinemas.size(), totalRooms);
    }

    /**
     * Seed Showtime: Mỗi phim 8-9 suất chiếu mỗi ngày, trong 7-14 ngày
     * Đảm bảo không xung đột thời gian trong cùng phòng.
     */
    @Transactional
    private void seedShowtimes() {
        log.info("Đang seed Showtimes (nhiều suất chiếu cho mỗi phim)...");

        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            log.warn("Không có room nào, bỏ qua seed showtimes");
            return;
        }

        // Seed showtime cho movies NOW_SHOWING và COMING_SOON
        List<Movie> availableMovies = movieRepository.findAll().stream()
                .filter(m -> m.getStatus() == MovieStatus.NOW_SHOWING || 
                            m.getStatus() == MovieStatus.COMING_SOON)
                .toList();

        if (availableMovies.isEmpty()) {
            log.warn("Không có movie NOW_SHOWING hoặc COMING_SOON, bỏ qua seed showtimes");
            return;
        }
        
        log.info("Tìm thấy {} movies để seed showtime (NOW_SHOWING + COMING_SOON)", availableMovies.size());

        LocalDate today = LocalDate.now();
        int daySpan = 14; // Seed trong 14 ngày
        int showsPerMoviePerDay = 8; // Mỗi phim 8 suất/ngày
        List<Showtime> showtimes = new ArrayList<>();

        // Time slots trong ngày (từ 9h đến 22h)
        List<LocalTime> baseSlots = List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 30),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30),
                LocalTime.of(18, 0),
                LocalTime.of(19, 30),
                LocalTime.of(21, 0),
                LocalTime.of(22, 30)
        );

        LocalDateTime now = LocalDateTime.now();

        // Với mỗi phim, seed showtime trong nhiều ngày
        for (Movie movie : availableMovies) {
            int durationMinutes = movie.getDuration() != null ? movie.getDuration() : 120;
            
            for (int d = 0; d < daySpan; d++) {
                LocalDate date = today.plusDays(d);
                
                // Bỏ qua ngày quá khứ (trừ hôm nay nếu còn thời gian)
                if (d == 0 && date.atTime(LocalTime.of(22, 0)).isBefore(now)) {
                    continue; // Hôm nay đã quá muộn
                }
                
                // Mỗi phim cần 8 suất chiếu mỗi ngày
                int showsCreatedForThisMovieToday = 0;
                List<Room> shuffledRooms = new ArrayList<>(rooms);
                Collections.shuffle(shuffledRooms, random);
                
                // Thử tạo showtime cho phim này trong ngày này
                for (Room room : shuffledRooms) {
                    if (showsCreatedForThisMovieToday >= showsPerMoviePerDay) {
                        break; // Đã đủ 8 suất cho phim này hôm nay
                    }
                    
                    // Thử các time slot
                    List<LocalTime> availableSlots = new ArrayList<>(baseSlots);
                    Collections.shuffle(availableSlots, random);
                    
                    for (LocalTime slot : availableSlots) {
                        if (showsCreatedForThisMovieToday >= showsPerMoviePerDay) {
                            break;
                        }
                        
                        LocalDateTime start = date.atTime(slot);
                        
                        // Bỏ qua nếu quá khứ (với buffer 30 phút)
                        if (start.isBefore(now.plusMinutes(30))) {
                            continue;
                        }
                        
                        // Tính end time (thêm 10 phút buffer dọn phòng)
                        LocalDateTime end = start.plusMinutes(durationMinutes + 10L);
                        
                        // Không để quá trễ (sau 23:30)
                        if (end.toLocalTime().isAfter(LocalTime.of(23, 30))) {
                            continue;
                        }
                        
                        // Check conflict trong room
                        boolean hasConflict = !showtimeRepository.findConflictingShowtimes(room.getId(), start, end).isEmpty();
                        if (hasConflict) {
                            continue;
                        }
                        
                        // Tạo showtime
                        Showtime showtime = new Showtime();
                        showtime.setMovie(movie);
                        showtime.setRoom(room);
                        showtime.setStartTime(start);
                        showtime.setEndTime(end);
                        
                        // Giá vé cố định: 75,000 VNĐ cho tất cả showtime
                        BigDecimal fixedPrice = BigDecimal.valueOf(75000);
                        showtime.setPrice(fixedPrice);
                        
                        showtimes.add(showtime);
                        showsCreatedForThisMovieToday++;
                    }
                }
            }
        }

        if (showtimes.isEmpty()) {
            log.warn("Không seed được showtime nào (có thể do conflict), bỏ qua");
            return;
        }

        showtimeRepository.saveAll(showtimes);
        log.info("Đã seed {} showtimes (trung bình {} suất/phim/ngày trong {} ngày)", 
                showtimes.size(), showsPerMoviePerDay, daySpan);
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

    private Cinema createCinema(String name, String address, String phone, String email) {
        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setPhone(phone);
        cinema.setEmail(email);
        return cinema;
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
     * Seed Reviews: 2-5 review mỗi movie
     */
    private void seedReviews() {
        log.info("Đang seed Reviews...");

        List<Movie> movies = movieRepository.findAll();
        List<User> users = userRepository.findAll();

        if (movies.isEmpty() || users.isEmpty()) {
            log.warn("Không có movie hoặc user, bỏ qua seed reviews");
            return;
        }

        if (reviewRepository.count() > 0) {
            log.info("Reviews đã có dữ liệu ({}), bỏ qua seed.", reviewRepository.count());
            return;
        }

        List<String> reviewComments = List.of(
                "Great movie! Highly recommended.",
                "Amazing storyline and acting.",
                "One of the best movies I've seen.",
                "Good but could be better.",
                "Not my favorite, but decent.",
                "Excellent cinematography!",
                "The plot was a bit confusing.",
                "Loved every minute of it!",
                "Great for a family watch.",
                "Action-packed and thrilling!"
        );

        List<Review> reviewsToSeed = new ArrayList<>();

        for (Movie movie : movies) {
            int numReviews = random.nextInt(4) + 2; // 2-5 reviews
            List<User> shuffledUsers = new ArrayList<>(users);
            Collections.shuffle(shuffledUsers, random);
            List<User> selectedUsers = shuffledUsers.subList(0, Math.min(numReviews, shuffledUsers.size()));

            for (User user : selectedUsers) {
                Review review = new Review();
                review.setMovie(movie);
                String authorName = user.getFullName() != null && !user.getFullName().isBlank()
                        ? user.getFullName()
                        : user.getUsername();
                review.setAuthorName(authorName);
                review.setRating(3 + random.nextInt(3)); // 3-5 stars
                review.setComment(reviewComments.get(random.nextInt(reviewComments.size())));
                reviewsToSeed.add(review);
            }
        }

        reviewRepository.saveAll(reviewsToSeed);
        log.info("Đã seed {} reviews cho {} movies", reviewsToSeed.size(), movies.size());
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