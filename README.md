# Cinema Booking System - Backend

Backend API cho hệ thống quản lý và đặt vé xem phim.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL**
- **Spring Security + JWT**
- **Lombok**

## Setup

### 1. Yêu cầu

- Java 17 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+

### 2. Cấu hình Database

1. Tạo database MySQL:
```sql
CREATE DATABASE cinema_db;
```

2. Cấu hình trong `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cinema_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Chạy ứng dụng

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Hoặc chạy từ IDE:
- Mở file `CinemaApplication.java`
- Run as Java Application

### 4. Kiểm tra

Ứng dụng chạy tại: `http://localhost:8080`

## Package Structure

```
com.cinema
├── controller      # API endpoints
├── service         # Business logic
├── repository      # Data access layer
├── model
│   ├── entity      # JPA entities
│   └── enum        # Enums
├── config          # Configuration
└── security        # Security & JWT
```

## API Endpoints

(Sẽ được cập nhật sau khi implement)

## Development

### Database Migration

Spring Boot tự động tạo/update database schema dựa vào Entities.

Cấu hình: `spring.jpa.hibernate.ddl-auto=update`

### Logging

SQL queries được log ra console (development mode).

## Notes

- JWT secret key cần được thay đổi trong production
- Database password cần được cấu hình đúng





