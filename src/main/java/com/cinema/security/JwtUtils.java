package com.cinema.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtUtils - tiện ích tạo và validate JWT token.
 * Sử dụng cấu hình từ application.properties: jwt.secret, jwt.expiration
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // Chấp nhận dùng API deprecated cho đơn giản trong project học tập
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    private Claims getAllClaimsFromToken(String token) {
        JwtParser parser = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build();

        return parser.parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token, String username) {
        final String usernameFromToken = getUsernameFromToken(token);
        final Date expiration = getExpirationDateFromToken(token);
        return (usernameFromToken.equals(username) && expiration.after(new Date()));
    }
}


