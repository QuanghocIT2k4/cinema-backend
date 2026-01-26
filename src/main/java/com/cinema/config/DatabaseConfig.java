package com.cinema.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Configuration để tự động detect database type từ DATABASE_URL
 * Render inject: DATABASE_URL (PostgreSQL)
 * Railway inject: MYSQL_URL hoặc DATABASE_URL
 * Tự động set driver class name và build URL nếu cần
 */
@Configuration
@Slf4j
@Order(1) // Chạy trước các config khác để override driver
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;
    
    @Value("${MYSQL_URL:}")
    private String mysqlUrl;
    
    @Value("${MYSQL_HOST:}")
    private String mysqlHost;
    
    @Value("${MYSQL_DATABASE:}")
    private String mysqlDatabase;
    
    @Value("${MYSQL_USER:}")
    private String mysqlUser;
    
    @Value("${MYSQL_PASSWORD:}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Build URL từ env vars
        String finalUrl = buildDatabaseUrl();
        
        // Nếu có DATABASE_URL hoặc MYSQL_URL → build DataSource từ đầu (override hoàn toàn)
        if (finalUrl != null && !finalUrl.isEmpty()) {
            String driverClassName = detectDriverFromUrl(finalUrl);
            
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(finalUrl);
            dataSource.setDriverClassName(driverClassName);
            
            // Set pool config
            dataSource.setMaximumPoolSize(10);
            dataSource.setMinimumIdle(2);
            dataSource.setConnectionTimeout(30000);
            
            log.info("=== Using Render/Railway/Production database config ===");
            log.info("Database URL: {}", finalUrl.replaceAll(":[^:@]+@", ":****@"));
            log.info("Auto-detected driver: {}", driverClassName);
            log.info("=== Overriding datasource from application.properties ===");
            
            return dataSource;
        }
        
        // Không có env vars → dùng default từ Spring Boot auto-config
        log.info("Using default Spring Boot datasource configuration from application.properties");
        return null; // Spring Boot sẽ tự động tạo DataSource từ properties
    }

    /**
     * Build database URL từ các biến riêng lẻ nếu DATABASE_URL không có
     */
    private String buildDatabaseUrl() {
        // Ưu tiên DATABASE_URL
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Nếu thiếu jdbc: prefix thì thêm vào
            if (!databaseUrl.startsWith("jdbc:")) {
                if (databaseUrl.startsWith("mysql://")) {
                    return "jdbc:" + databaseUrl;
                }
                if (databaseUrl.startsWith("postgresql://")) {
                    return "jdbc:" + databaseUrl;
                }
            }
            return databaseUrl;
        }
        
        // Nếu có MYSQL_URL
        if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
            if (!mysqlUrl.startsWith("jdbc:")) {
                return "jdbc:" + mysqlUrl;
            }
            return mysqlUrl;
        }
        
        // Build từ các biến riêng lẻ
        if (mysqlHost != null && !mysqlHost.isEmpty() && 
            mysqlDatabase != null && !mysqlDatabase.isEmpty()) {
            String url = String.format("jdbc:mysql://%s:3306/%s", mysqlHost, mysqlDatabase);
            if (mysqlUser != null && !mysqlUser.isEmpty() && 
                mysqlPassword != null && !mysqlPassword.isEmpty()) {
                url += String.format("?user=%s&password=%s", mysqlUser, mysqlPassword);
            }
            return url;
        }
        
        return null; // Sẽ dùng default từ application.properties
    }

    /**
     * Tự động detect driver class name từ database URL
     */
    private String detectDriverFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "com.mysql.cj.jdbc.Driver"; // Default MySQL
        }
        
        // Nếu URL chứa "postgresql" → dùng PostgreSQL driver
        if (url.contains("postgresql")) {
            return "org.postgresql.Driver";
        }
        
        // Nếu URL chứa "mysql" → dùng MySQL driver
        if (url.contains("mysql")) {
            return "com.mysql.cj.jdbc.Driver";
        }
        
        // Default: MySQL
        return "com.mysql.cj.jdbc.Driver";
    }
}
