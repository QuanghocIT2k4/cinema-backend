# HƯỚNG DẪN TEST KẾT NỐI MYSQL VÀ CRUD CƠ BẢN

**Mục đích:** Test kết nối MySQL và các thao tác CRUD cơ bản với User entity

---

## 📋 YÊU CẦU

1. MySQL đã được cài đặt và đang chạy
2. Database `cinema_db` sẽ được tự động tạo khi chạy Spring Boot
3. Port 8080 chưa bị sử dụng

---

## 🚀 CÁCH CHẠY

### Bước 1: Chạy Spring Boot Application

```bash
cd cinema-backend
./mvnw spring-boot:run
```

Hoặc nếu dùng IDE (IntelliJ IDEA, Eclipse):
- Click chuột phải vào `CinemaApplication.java`
- Chọn "Run 'CinemaApplication'"

### Bước 2: Kiểm tra Application đã chạy

Mở browser hoặc Postman, truy cập:
```
http://localhost:8080/api/test/db
```

**Kết quả mong đợi:**
```json
{
  "status": "success",
  "message": "Database connection successful",
  "userCount": 0
}
```

---

## 🧪 TEST CÁC API ENDPOINTS

### 1. Test Database Connection

**GET** `http://localhost:8080/api/test/db`

**Response:**
```json
{
  "status": "success",
  "message": "Database connection successful",
  "userCount": 0
}
```

---

### 2. Test CREATE - Tạo User mới

**POST** `http://localhost:8080/api/test/users`

**Request Body:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "User created successfully",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

---

### 3. Test READ - Lấy tất cả Users

**GET** `http://localhost:8080/api/test/users`

**Response:**
```json
{
  "status": "success",
  "message": "Users retrieved successfully",
  "count": 1,
  "users": [
    {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    }
  ]
}
```

---

### 4. Test READ - Lấy User theo ID

**GET** `http://localhost:8080/api/test/users/1`

**Response:**
```json
{
  "status": "success",
  "message": "User found",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "fullName": "Test User"
  }
}
```

---

### 5. Test READ - Tìm User theo Username (Repository method)

**GET** `http://localhost:8080/api/test/users/username/testuser`

**Response:**
```json
{
  "status": "success",
  "message": "User found",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

---

### 6. Test UPDATE - Cập nhật User

**PUT** `http://localhost:8080/api/test/users/1`

**Request Body:**
```json
{
  "fullName": "Updated Test User",
  "phone": "0123456789"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "User updated successfully",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Updated Test User"
  }
}
```

---

### 7. Test DELETE - Xóa User

**DELETE** `http://localhost:8080/api/test/users/1`

**Response:**
```json
{
  "status": "success",
  "message": "User deleted successfully"
}
```

---

## ✅ CHECKLIST TEST

- [ ] Test database connection thành công
- [ ] Test CREATE user thành công
- [ ] Test READ all users thành công
- [ ] Test READ user by ID thành công
- [ ] Test READ user by username thành công (Repository method)
- [ ] Test UPDATE user thành công
- [ ] Test DELETE user thành công
- [ ] Kiểm tra database có dữ liệu đúng không (dùng MySQL Workbench hoặc command line)

---

## 🔍 KIỂM TRA DATABASE

### Sử dụng MySQL Command Line:

```sql
-- Kết nối MySQL
mysql -u root -p

-- Chọn database
USE cinema_db;

-- Xem tất cả tables
SHOW TABLES;

-- Xem dữ liệu trong bảng users
SELECT * FROM users;

-- Xem cấu trúc bảng users
DESCRIBE users;
```

### Sử dụng MySQL Workbench:

1. Mở MySQL Workbench
2. Kết nối đến localhost:3306
3. Chọn database `cinema_db`
4. Xem dữ liệu trong bảng `users`

---

## ⚠️ LƯU Ý

1. **TestController chỉ dùng để test** - Sẽ được xóa sau khi test xong
2. **Password chưa được hash** - Trong test này password được lưu plain text (không an toàn)
3. **Security tạm thời disabled** - Chỉ cho phép `/api/test/**` không cần authentication
4. **Sau khi test xong** - Xóa TestController và cập nhật SecurityConfig

---

## 🐛 XỬ LÝ LỖI

### Lỗi: "Cannot connect to database"
- Kiểm tra MySQL đã chạy chưa
- Kiểm tra username/password trong `application.properties`
- Kiểm tra port 3306 có đúng không

### Lỗi: "Table 'users' doesn't exist"
- Kiểm tra `spring.jpa.hibernate.ddl-auto=update` trong `application.properties`
- Restart application để Hibernate tự động tạo tables

### Lỗi: "Port 8080 already in use"
- Đổi port trong `application.properties`: `server.port=8081`
- Hoặc tắt ứng dụng đang dùng port 8080

---

## 📝 KẾT QUẢ MONG ĐỢI

Sau khi test thành công:
- ✅ Database `cinema_db` được tạo tự động
- ✅ Bảng `users` được tạo tự động
- ✅ Có thể thực hiện CRUD operations thành công
- ✅ Repository methods hoạt động đúng

**Sau đó có thể xóa TestController và tiếp tục với Tuần 2!**

