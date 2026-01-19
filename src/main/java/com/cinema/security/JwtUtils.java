package com.cinema.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtils - tiện ích tạo và validate JWT token.
 *
 * BƯỚC 2 theo roadmap:
 * - Đọc cấu hình từ application.properties: jwt.secret, jwt.expiration.
 * - Cung cấp các hàm:
 *   + generateToken(username)      : tạo JWT với subject = username.
 *   + getUsernameFromToken(token)  : đọc lại username từ token.
 *   + validateToken(token, user)   : kiểm tra token còn hạn & đúng user.
 *
 * Lưu ý:
 * - Dùng thư viện jjwt 0.12.x (đã khai báo trong pom.xml).
 * - Secret key phải đủ mạnh (>= 32 bytes cho HS256) → xem jwt.secret.
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;          // Chuỗi bí mật dùng để ký token

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;      // Thời gian sống của token (milliseconds)

    /**
     * Tạo SecretKey từ chuỗi secret trong cấu hình.
     * - Ở đây dùng trực tiếp bytes UTF-8 của chuỗi secret.
     * - YÊU CẦU: độ dài chuỗi >= 32 bytes để dùng cho HS256.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo JWT token từ username.
     * - subject: username
     * - issuedAt: thời điểm tạo
     * - expiration: now + jwtExpirationMs
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey()) // HS256 mặc định với SecretKey HMAC
                .compact();
    }

    /**
     * Đọc username (subject) từ token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Đọc thời điểm hết hạn từ token.
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Parse và trả về toàn bộ Claims từ token.
     * - Nếu token không hợp lệ / sai chữ ký / format sai → ném JwtException.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate token:
     * - Chữ ký hợp lệ (parse được claims với signing key).
     * - Username trong token trùng với username truyền vào.
     * - Token chưa hết hạn.
     */
    public boolean validateToken(String token, String username) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String usernameFromToken = claims.getSubject();
            Date expiration = claims.getExpiration();

            return usernameFromToken.equals(username) && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            // Token sai chữ ký, hết hạn, hoặc format không đúng
            return false;
        }
    }
}


