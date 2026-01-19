# Cinema Booking System - Backend

Backend API cho hệ thống quản lý và đặt vé xem phim.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **MySQL 8.0** (chạy bằng Docker)
- **Spring Security + JWT**
- **Lombok**

## Setup

### 1. Yêu cầu

- Java 17 hoặc cao hơn
- Maven 3.6+ (hoặc dùng Maven Wrapper `mvnw.cmd`)
- **Docker Desktop** (để chạy MySQL)

### 2. Cấu hình Database (Docker)

#### Bước 1: Chạy MySQL bằng Docker

```bash
cd cinema-backend
docker-compose up -d
```

Lệnh này sẽ:
- Tải MySQL 8.0 image (nếu chưa có)
- Tạo container `cinema-mysql`
- Tạo database `cinema_db` tự động
- Expose port `3306` để Spring Boot kết nối

#### Bước 2: Kiểm tra MySQL đang chạy

```bash
docker ps
```

Bạn sẽ thấy container `cinema-mysql` đang chạy.

#### Bước 3: Kết nối MySQL (tùy chọn)

- **MySQL Workbench:**
  - Host: `localhost`
  - Port: `3306`
  - Username: `root`
  - Password: `rootpassword`

- **Command line:**
```bash
docker exec -it cinema-mysql mysql -u root -prootpassword
```

### 3. Cấu hình Application

File `application.properties` đã được cấu hình sẵn với:
- Database URL: `jdbc:mysql://localhost:3306/cinema_db`
- Username: `root`
- Password: `rootpassword` (từ docker-compose.yml)

Nếu muốn override, set biến môi trường:
```bash
$env:DB_PASSWORD="your_password"
```

### 4. Chạy Application

```bash
cd cinema-backend
.\mvnw.cmd spring-boot:run
```

Hoặc dùng script:
```bash
.\run-be.bat
```

### 5. Kiểm tra Application đang chạy

- Mở browser: `http://localhost:8080/api/test/db`
- Nếu thấy JSON response → Backend đã kết nối MySQL thành công!

## Docker Commands

### Dừng MySQL:
```bash
docker-compose down
```

### Xem logs MySQL:
```bash
docker-compose logs mysql
```

### Xóa container và data (reset hoàn toàn):
```bash
docker-compose down -v
```

### Khởi động lại MySQL:
```bash
docker-compose restart mysql
```

## API Endpoints

### Auth
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập (trả về JWT token)
- `GET /api/auth/me` - Lấy thông tin user hiện tại

### Test
- `GET /api/test/db` - Test kết nối database

## Lưu ý

- **Docker phải đang chạy** trước khi chạy Spring Boot
- Nếu port 3306 đã bị chiếm, sửa port trong `docker-compose.yml`:
  ```yaml
  ports:
    - "3307:3306"  # Thay đổi port host
  ```
  Và cập nhật `application.properties`:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3307/cinema_db...
  ```
