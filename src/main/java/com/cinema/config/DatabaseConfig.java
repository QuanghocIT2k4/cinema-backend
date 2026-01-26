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
 * Configuration để tự động convert DATABASE_URL từ Render (postgresql://...)
 * sang format JDBC (jdbc:postgresql://...)
 * 
 * Render cung cấp: postgresql://user:pass@host/dbname
 * Spring Boot cần: jdbc:postgresql://user:pass@host/dbname
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
        String jdbcUrl = convertToJdbcUrl(datasourceUrl);
        properties.setUrl(jdbcUrl);
        log.info("Database URL converted: {} -> {}", 
                datasourceUrl != null ? datasourceUrl.replaceAll(":[^:@]+@", ":****@") : "null",
                jdbcUrl != null ? jdbcUrl.replaceAll(":[^:@]+@", ":****@") : "null");
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Convert DATABASE_URL từ Render format (postgresql://...) 
     * sang JDBC format (jdbc:postgresql://...)
     */
    private String convertToJdbcUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // Nếu đã có jdbc: prefix thì giữ nguyên
        if (url.startsWith("jdbc:")) {
            return url;
        }
        
        // Nếu bắt đầu bằng postgresql:// thì thêm jdbc: prefix
        if (url.startsWith("postgresql://")) {
            return "jdbc:" + url;
        }
        
        // Nếu bắt đầu bằng postgres:// thì convert sang jdbc:postgresql://
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        
        // Các trường hợp khác giữ nguyên
        return url;
    }
}

