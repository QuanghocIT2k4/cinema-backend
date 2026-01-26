package com.cinema.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration để tự động detect database type từ DATABASE_URL hoặc MYSQL_URL
 * Railway MySQL inject: MYSQL_URL hoặc DATABASE_URL
 * Tự động set driver class name và build URL nếu cần
 */
@Configuration
@Slf4j
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
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        // CHỈ override khi có env vars từ Railway/Production
        // Local sẽ dùng default từ application.properties
        String finalUrl = buildDatabaseUrl();
        
        if (finalUrl != null && !finalUrl.isEmpty()) {
            // Có env vars → override
            String driverClassName = detectDriverFromUrl(finalUrl);
            properties.setUrl(finalUrl);
            properties.setDriverClassName(driverClassName);
            log.info("Using Railway/Production database config");
            log.info("Database URL: {}", finalUrl.replaceAll(":[^:@]+@", ":****@"));
            log.info("Auto-detected driver: {}", driverClassName);
        } else {
            // Không có env vars → dùng default từ application.properties
            log.info("Using local database config from application.properties");
        }
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
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