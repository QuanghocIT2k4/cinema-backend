package com.cinema.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration để tự động convert DATABASE_URL từ Render (postgresql://...)
 * sang format JDBC (jdbc:postgresql://...)
 * 
 * Render cung cấp: postgresql://user:pass@host/dbname
 * Spring Boot cần: jdbc:postgresql://user:pass@host/dbname
 * 
 * Class này sẽ tự động thêm jdbc: prefix nếu thiếu
 */
@Component
@Slf4j
public class DatabaseConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        String databaseUrl = environment.getProperty("DATABASE_URL");
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            String jdbcUrl = convertToJdbcUrl(databaseUrl);
            
            if (!jdbcUrl.equals(databaseUrl)) {
                log.info("Converting DATABASE_URL: {} -> {}", 
                        databaseUrl.replaceAll(":[^:@]+@", ":****@"),
                        jdbcUrl.replaceAll(":[^:@]+@", ":****@"));
                
                // Override spring.datasource.url với JDBC URL
                Map<String, Object> properties = new HashMap<>();
                properties.put("spring.datasource.url", jdbcUrl);
                
                environment.getPropertySources().addFirst(
                        new MapPropertySource("database-url-override", properties)
                );
            }
        }
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

