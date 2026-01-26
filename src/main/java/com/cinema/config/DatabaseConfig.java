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
 * Configuration để tự động detect database type từ DATABASE_URL
 * Railway inject DATABASE_URL dạng: jdbc:postgresql://... hoặc jdbc:mysql://...
 * Tự động set driver class name phù hợp
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        // Auto-detect driver và dialect từ URL
        String driverClassName = detectDriverFromUrl(datasourceUrl);
        properties.setDriverClassName(driverClassName);
        
        log.info("Database URL: {}", datasourceUrl != null ? datasourceUrl.replaceAll(":[^:@]+@", ":****@") : "null");
        log.info("Auto-detected driver: {}", driverClassName);
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Tự động detect driver class name từ database URL
     */
    private String detectDriverFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "org.postgresql.Driver"; // Default cho Railway
        }
        
        // Nếu URL chứa "postgresql" → dùng PostgreSQL driver
        if (url.contains("postgresql")) {
            return "org.postgresql.Driver";
        }
        
        // Nếu URL chứa "mysql" → dùng MySQL driver
        if (url.contains("mysql")) {
            return "com.mysql.cj.jdbc.Driver";
        }
        
        // Default: PostgreSQL (Railway thường dùng PostgreSQL)
        return "org.postgresql.Driver";
    }
}

