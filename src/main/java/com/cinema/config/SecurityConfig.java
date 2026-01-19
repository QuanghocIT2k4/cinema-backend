package com.cinema.config;

import com.cinema.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * CẤU HÌNH SPRING SECURITY (BƯỚC 1 - CHƯA GẮN JWT)
 *
 * Mục tiêu:
 * - Bật Spring Security cho ứng dụng.
 * - Định nghĩa kiểu session (stateless để chuẩn bị dùng JWT).
 * - Cấu hình rule cơ bản cho HTTP request.
 * - Cấp sẵn PasswordEncoder + AuthenticationManager cho các bước Auth sau.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SecurityFilterChain: định nghĩa toàn bộ luật bảo mật cho HTTP request.
     *
     * Hiện tại:
     * - Tắt CSRF (sẽ dùng JWT, không dùng form login + session).
     * - Đặt SessionCreationPolicy.STATELESS để chuẩn bị cho kiến trúc JWT.
     * - CHƯA chặn endpoint nào (anyRequest().permitAll()) để dễ dev,
     *   các bước sau khi có JWT + phân quyền sẽ siết dần lại.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Dùng JWT stateless nên tạm thời tắt CSRF cho đơn giản
                .csrf(csrf -> csrf.disable())
                // Enable CORS for frontend dev server (Vite)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Chuẩn bị cho kiến trúc JWT: server không lưu session login
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Cho phép public endpoints không cần authentication
                        .requestMatchers("/api").permitAll() // API info endpoint
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // GET movies public (chỉ GET method)
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        // POST/PUT/DELETE movies cần authentication (check Admin trong Service)
                        .requestMatchers(HttpMethod.POST, "/api/movies", "/api/movies/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").authenticated()
                        // GET cinemas public (chỉ GET method)
                        .requestMatchers(HttpMethod.GET, "/api/cinemas/**").permitAll()
                        // POST/PUT/DELETE cinemas cần authentication (check Admin trong Service)
                        .requestMatchers(HttpMethod.POST, "/api/cinemas", "/api/cinemas/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/cinemas/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/cinemas/**").authenticated()
                        // GET rooms public (chỉ GET method)
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        // POST/PUT/DELETE rooms cần authentication (check Admin trong Service)
                        .requestMatchers(HttpMethod.POST, "/api/rooms", "/api/rooms/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").authenticated()
                        // Các endpoint khác cần authentication
                        .anyRequest().authenticated()
                )
                // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration to allow frontend (Vite) to call API during development and production.
     * Reads allowed origins from environment variable ALLOWED_ORIGINS (comma-separated),
     * or defaults to localhost for development.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Đọc từ biến môi trường ALLOWED_ORIGINS (cho production)
        String allowedOriginsEnv = System.getenv("ALLOWED_ORIGINS");
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            // Split bằng comma và trim whitespace
            List<String> origins = List.of(allowedOriginsEnv.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            config.setAllowedOrigins(origins);
        } else {
            // Default cho development
            config.setAllowedOrigins(List.of(
                    "http://localhost:5173",
                    "http://127.0.0.1:5173"
            ));
        }
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * PasswordEncoder dùng BCrypt để hash mật khẩu.
     * Sẽ được dùng ở bước Auth (register/login) sau này.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager để sau này AuthService có thể gọi authenticate()
     * dựa trên UserDetailsService + PasswordEncoder đã cấu hình.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}


