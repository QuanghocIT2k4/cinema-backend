package com.cinema.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 *
 * BƯỚC 4 theo roadmap: Setup JWT authentication filter.
 *
 * Nhiệm vụ:
 * - Đọc JWT token từ header "Authorization: Bearer <token>".
 * - Validate token (chữ ký, hết hạn, username).
 * - Nếu hợp lệ: load User từ DB → set Authentication vào SecurityContext.
 * - Cho phép request đi tiếp vào Controller (hoặc bị chặn nếu không hợp lệ).
 *
 * Filter này chạy TRƯỚC UsernamePasswordAuthenticationFilter (filter mặc định của Spring).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    /**
     * doFilterInternal: method chính của filter, được gọi cho MỖI request HTTP.
     *
     * Flow:
     * 1. Đọc token từ header Authorization.
     * 2. Nếu có token → parse username từ token.
     * 3. Nếu username hợp lệ + SecurityContext chưa có Authentication:
     *    - Load UserDetails từ DB (CustomUserDetailsService).
     *    - Validate token với username đó.
     *    - Nếu hợp lệ → tạo Authentication → set vào SecurityContext.
     * 4. Cho request đi tiếp (filterChain.doFilter).
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Bước 1: Đọc JWT token từ header "Authorization: Bearer <token>"
            String jwt = parseJwt(request);

            if (jwt != null) {
                // Bước 2: Lấy username từ token
                String username = jwtUtils.getUsernameFromToken(jwt);
                log.debug("JWT Token parsed, username: {}", username);

                // Bước 3: Nếu có username + SecurityContext chưa có Authentication
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        // Load UserDetails từ DB (lấy role, status mới nhất)
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        log.debug("UserDetails loaded for username: {}", username);

                        // Bước 4: Validate token với username đó
                        if (jwtUtils.validateToken(jwt, username)) {
                            // Bước 5: Tạo Authentication object và set vào SecurityContext
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null, // credentials = null (đã verify rồi)
                                            userDetails.getAuthorities() // role/permissions
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // Set vào SecurityContext → các filter/controller sau có thể dùng
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Authentication set for username: {}", username);
                        } else {
                            log.warn("Token validation failed for username: {}", username);
                        }
                    } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                        // User không tồn tại trong DB
                        log.error("User not found in database: {}", username);
                        // KHÔNG set authentication, để SecurityContext trống
                    }
                }
            }
        } catch (Exception ex) {
            // Log lỗi nhưng không chặn request (có thể là request không có token, public endpoint)
            log.error("Cannot set user authentication: {}", ex.getMessage(), ex);
        }

        // Bước 6: Cho request đi tiếp vào filter chain (hoặc vào Controller nếu đã qua hết filter)
        filterChain.doFilter(request, response);
    }

    /**
     * parseJwt: Đọc token từ header "Authorization: Bearer <token>".
     *
     * @param request HTTP request
     * @return JWT token string (không có prefix "Bearer "), hoặc null nếu không có token
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Kiểm tra header có format "Bearer <token>" không
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Cắt bỏ prefix "Bearer " → chỉ lấy token
            return headerAuth.substring(7);
        }

        return null;
    }
}

