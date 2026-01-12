# HƯỚNG DẪN TEST AUTH API - NGÀY 2 TUẦN 2

## 🎯 Mục tiêu
Test 3 API endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/me`

---

## 📋 CHUẨN BỊ

### **1. Chạy Backend:**
```bash
cd cinema-backend
.\mvnw.cmd spring-boot:run
```

Hoặc dùng file batch:
```bash
.\run-be.bat
```

Đợi đến khi thấy log: `Started CinemaApplication`

### **2. Mở Postman:**
- Tạo Collection mới: `Cinema Backend - Auth API`
- Base URL: `http://localhost:8080`

---

## ✅ TEST 1: ĐĂNG KÝ (POST /api/auth/register)

### **Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/register`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
```json
{
  "username": "customer1",
  "email": "customer1@example.com",
  "password": "password123",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789"
}
```

### **Response mong đợi (201 Created):**
```json
{
  "id": 1,
  "username": "customer1",
  "email": "customer1@example.com",
  "role": "CUSTOMER",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "address": null,
  "avatar": null,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### **Test Cases:**

#### ✅ **Test Case 1.1: Đăng ký thành công**
- Gửi request với dữ liệu hợp lệ
- **Kỳ vọng:** Status 201, trả về UserResponse (không có password)

#### ❌ **Test Case 1.2: Username đã tồn tại**
- Gửi request với username đã tồn tại
- **Kỳ vọng:** Status 400, message: "Username đã tồn tại"

#### ❌ **Test Case 1.3: Email đã tồn tại**
- Gửi request với email đã tồn tại
- **Kỳ vọng:** Status 400, message: "Email đã được sử dụng"

#### ❌ **Test Case 1.4: Validation lỗi - Username quá ngắn**
- Gửi request với `username: "ab"` (dưới 3 ký tự)
- **Kỳ vọng:** Status 400, validation error

#### ❌ **Test Case 1.5: Validation lỗi - Email không hợp lệ**
- Gửi request với `email: "invalid-email"`
- **Kỳ vọng:** Status 400, validation error

#### ❌ **Test Case 1.6: Validation lỗi - Password quá ngắn**
- Gửi request với `password: "123"` (dưới 6 ký tự)
- **Kỳ vọng:** Status 400, validation error

---

## ✅ TEST 2: ĐĂNG NHẬP (POST /api/auth/login)

### **Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
```json
{
  "username": "customer1",
  "password": "password123"
}
```

### **Response mong đợi (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "customer1",
    "email": "customer1@example.com",
    "role": "CUSTOMER",
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "status": "ACTIVE"
  }
}
```

### **Test Cases:**

#### ✅ **Test Case 2.1: Đăng nhập thành công**
- Gửi request với username/password đúng (đã đăng ký ở Test 1)
- **Kỳ vọng:** Status 200, trả về `token` + `user`
- **Lưu token** để dùng cho Test 3

#### ❌ **Test Case 2.2: Sai username**
- Gửi request với `username: "wronguser"`
- **Kỳ vọng:** Status 401, message: "Username hoặc mật khẩu không đúng"

#### ❌ **Test Case 2.3: Sai password**
- Gửi request với `password: "wrongpass"`
- **Kỳ vọng:** Status 401, message: "Username hoặc mật khẩu không đúng"

#### ❌ **Test Case 2.4: Validation lỗi - Username trống**
- Gửi request với `username: ""`
- **Kỳ vọng:** Status 400, validation error

---

## ✅ TEST 3: LẤY THÔNG TIN USER HIỆN TẠI (GET /api/auth/me)

### **Request:**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/auth/me`
- **Headers:**
  ```
  Authorization: Bearer <token>
  ```
  (Thay `<token>` bằng token nhận được từ Test 2)

### **Response mong đợi (200 OK):**
```json
{
  "id": 1,
  "username": "customer1",
  "email": "customer1@example.com",
  "role": "CUSTOMER",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "address": null,
  "avatar": null,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### **Test Cases:**

#### ✅ **Test Case 3.1: Lấy thông tin thành công**
- Gửi request với token hợp lệ (từ Test 2)
- **Kỳ vọng:** Status 200, trả về UserResponse

#### ❌ **Test Case 3.2: Không có token**
- Gửi request **không có** header `Authorization`
- **Kỳ vọng:** Status 401, Unauthorized

#### ❌ **Test Case 3.3: Token không hợp lệ**
- Gửi request với `Authorization: Bearer invalid-token`
- **Kỳ vọng:** Status 401, Unauthorized

#### ❌ **Test Case 3.4: Token hết hạn**
- (Cần đợi token hết hạn hoặc tạo token với expiration ngắn để test)
- **Kỳ vọng:** Status 401, Unauthorized

---

## 🔄 FLOW TEST HOÀN CHỈNH

### **Bước 1: Đăng ký**
```
POST /api/auth/register
→ Nhận UserResponse (id, username, email, ...)
```

### **Bước 2: Đăng nhập**
```
POST /api/auth/login
→ Nhận token + user info
→ Lưu token vào biến Postman hoặc copy
```

### **Bước 3: Lấy thông tin user**
```
GET /api/auth/me
Headers: Authorization: Bearer <token>
→ Nhận UserResponse
```

---

## 📝 POSTMAN COLLECTION SETUP

### **Tạo Environment Variables:**
1. Trong Postman, tạo Environment: `Cinema Local`
2. Thêm biến:
   - `base_url`: `http://localhost:8080`
   - `token`: (để trống, sẽ set sau khi login)

### **Setup Pre-request Script cho /api/auth/login:**
```javascript
// Không cần gì đặc biệt
```

### **Setup Tests Script cho /api/auth/login:**
```javascript
// Lưu token vào environment variable
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.environment.set("token", response.token);
    console.log("Token đã được lưu:", response.token);
}
```

### **Setup Pre-request Script cho /api/auth/me:**
```javascript
// Tự động thêm token vào header
const token = pm.environment.get("token");
if (token) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + token
    });
}
```

---

## ✅ CHECKLIST HOÀN THÀNH

- [ ] Test Case 1.1: Đăng ký thành công
- [ ] Test Case 1.2: Username đã tồn tại
- [ ] Test Case 1.3: Email đã tồn tại
- [ ] Test Case 1.4: Validation - Username quá ngắn
- [ ] Test Case 1.5: Validation - Email không hợp lệ
- [ ] Test Case 1.6: Validation - Password quá ngắn
- [ ] Test Case 2.1: Đăng nhập thành công
- [ ] Test Case 2.2: Sai username
- [ ] Test Case 2.3: Sai password
- [ ] Test Case 2.4: Validation - Username trống
- [ ] Test Case 3.1: Lấy thông tin thành công
- [ ] Test Case 3.2: Không có token
- [ ] Test Case 3.3: Token không hợp lệ

---

## 🎯 KẾT QUẢ MONG ĐỢI

Sau khi test xong, bạn PHẢI có:
- ✅ **3 API endpoints** hoạt động đầy đủ
- ✅ **Tất cả test cases** pass
- ✅ **Token được lưu** và dùng được cho `/api/auth/me`
- ✅ **Error handling** hoạt động đúng (validation, duplicate, unauthorized)

---

**Ghi chú:** File này dùng để test và verify API đã code đúng. Sau khi test xong, bạn có thể tiếp tục Ngày 3 (CRUD User & Movie).

