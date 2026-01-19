# NGÀY 3: TỔNG KẾT KIẾN THỨC & CÂU HỎI

## 📚 KIẾN THỨC CẦN NẮM

### 1. **SPRING SECURITY CƠ BẢN**

#### 1.1. SecurityFilterChain
- **Mục đích:** Định nghĩa toàn bộ luật bảo mật cho HTTP request
- **Các cấu hình quan trọng:**
  - `csrf().disable()`: Tắt CSRF khi dùng JWT (stateless)
  - `sessionManagement().sessionCreationPolicy(STATELESS)`: Không tạo session
  - `authorizeHttpRequests()`: Cấu hình quyền truy cập endpoints
  - `addFilterBefore()`: Thêm custom filter vào filter chain

#### 1.2. PasswordEncoder
- **BCryptPasswordEncoder:** Hash password một chiều
- **encode():** Hash password trước khi lưu DB
- **matches():** So sánh password plain text với password đã hash

#### 1.3. AuthenticationManager
- **Mục đích:** Xác thực user (username + password)
- **authenticate():** Nhận UsernamePasswordAuthenticationToken, trả về Authentication object

---

### 2. **JWT (JSON WEB TOKEN)**

#### 2.1. Cấu trúc JWT
```
Header.Payload.Signature
```
- **Header:** Thuật toán mã hóa (HS256)
- **Payload:** Dữ liệu (subject/username, issuedAt, expiration)
- **Signature:** Chữ ký để verify token

#### 2.2. JwtUtils - Các method quan trọng
- **generateToken(username):** Tạo JWT token từ username
- **getUsernameFromToken(token):** Lấy username từ token
- **validateToken(token, username):** Kiểm tra token hợp lệ và chưa hết hạn
- **getExpirationDateFromToken(token):** Lấy thời gian hết hạn

#### 2.3. Secret Key
- **Yêu cầu:** Độ dài >= 32 bytes cho HS256
- **Lưu trữ:** Trong `application.properties` (jwt.secret)
- **Bảo mật:** Không commit secret key thật lên Git

---

### 3. **CÁC COMPONENT CHÍNH**

#### 3.1. CustomUserDetailsService
- **Interface:** `UserDetailsService`
- **Method:** `loadUserByUsername(username)`
- **Nhiệm vụ:** Load user từ DB và convert sang UserDetails
- **Return:** CustomUserDetails (chứa User entity + authorities/roles)

#### 3.2. JwtAuthenticationFilter
- **Extends:** `OncePerRequestFilter`
- **Nhiệm vụ:**
  1. Đọc token từ header `Authorization: Bearer <token>`
  2. Parse username từ token
  3. Load UserDetails từ DB
  4. Validate token
  5. Set Authentication vào SecurityContext
- **Quan trọng:** Phải được add vào SecurityFilterChain bằng `addFilterBefore()`

#### 3.3. SecurityContext
- **Mục đích:** Lưu thông tin authentication của request hiện tại
- **getAuthentication():** Lấy Authentication object
- **getName():** Lấy username từ Authentication

---

### 4. **FLOW AUTHENTICATION**

#### 4.1. Register Flow
```
1. Client gửi RegisterRequest (username, email, password, ...)
2. AuthService.register():
   - Kiểm tra username/email đã tồn tại chưa
   - Hash password bằng PasswordEncoder
   - Tạo User entity
   - Lưu vào DB
   - Convert sang UserResponse (không có password)
3. Return UserResponse (201 Created)
```

#### 4.2. Login Flow
```
1. Client gửi LoginRequest (username, password)
2. AuthService.login():
   - AuthenticationManager.authenticate() → verify username/password
   - Load User từ DB
   - Kiểm tra status (ACTIVE)
   - Generate JWT token bằng JwtUtils
   - Convert User sang UserResponse
3. Return AuthResponse (token + user) (200 OK)
```

#### 4.3. Get Me Flow (Protected Endpoint)
```
1. Client gửi request với header: Authorization: Bearer <token>
2. JwtAuthenticationFilter:
   - Parse token từ header
   - Lấy username từ token
   - Load UserDetails từ DB
   - Validate token
   - Set Authentication vào SecurityContext
3. AuthController.getCurrentUser():
   - Lấy Authentication từ SecurityContext
   - Lấy username từ Authentication
   - Load User từ DB
   - Convert sang UserResponse
4. Return UserResponse (200 OK)
```

---

### 5. **VALIDATION**

#### 5.1. Annotations
- **@Valid:** Kích hoạt validation trên DTO
- **@NotBlank:** Không được để trống
- **@Email:** Phải là email hợp lệ
- **@Size(min, max):** Độ dài trong khoảng
- **@Pattern(regexp):** Phải match regex pattern

#### 5.2. Custom Error Messages
- Message trong annotation: `@NotBlank(message = "Username không được để trống")`
- GlobalExceptionHandler: Bắt `MethodArgumentNotValidException` và format response

---

## ❓ DANH SÁCH CÂU HỎI CẦN NẮM

### **PHẦN 1: SPRING SECURITY**

**Q1: Tại sao phải tắt CSRF khi dùng JWT?**
- **Trả lời:** CSRF bảo vệ form-based authentication. JWT là stateless, không dùng session/cookie, nên không cần CSRF protection.

**Q2: SessionCreationPolicy.STATELESS là gì?**
- **Trả lời:** Không tạo session trên server. Mỗi request độc lập, authentication info nằm trong JWT token.

**Q3: PasswordEncoder.encode() và matches() khác nhau như thế nào?**
- **Trả lời:**
  - `encode()`: Hash password một chiều (dùng khi register)
  - `matches(rawPassword, encodedPassword)`: So sánh password plain text với password đã hash (dùng khi login)

**Q4: AuthenticationManager.authenticate() làm gì?**
- **Trả lời:** 
  - Nhận UsernamePasswordAuthenticationToken (username + password)
  - Load UserDetails từ UserDetailsService
  - So sánh password bằng PasswordEncoder.matches()
  - Trả về Authentication object nếu thành công

---

### **PHẦN 2: JWT**

**Q5: JWT token gồm những phần nào?**
- **Trả lời:** 3 phần phân cách bởi dấu `.`:
  - Header: Thuật toán (HS256)
  - Payload: Dữ liệu (subject/username, issuedAt, expiration)
  - Signature: Chữ ký để verify

**Q6: Tại sao cần validate token trước khi set Authentication?**
- **Trả lời:** 
  - Kiểm tra token chưa hết hạn
  - Kiểm tra username trong token khớp với username truyền vào
  - Kiểm tra chữ ký hợp lệ (token không bị giả mạo)

**Q7: Secret key phải có độ dài tối thiểu bao nhiêu?**
- **Trả lời:** 32 bytes (256 bits) cho thuật toán HS256

**Q8: Token hết hạn thì làm sao?**
- **Trả lời:** Client phải login lại để lấy token mới. Có thể implement refresh token (nâng cao).

---

### **PHẦN 3: FILTER & SECURITY CONTEXT**

**Q9: JwtAuthenticationFilter chạy khi nào?**
- **Trả lời:** Chạy cho MỌI request HTTP, trước khi đến Controller. Filter được add vào SecurityFilterChain.

**Q10: Tại sao phải add JwtAuthenticationFilter vào SecurityConfig?**
- **Trả lời:** 
  - Nếu không add, filter không chạy
  - Token không được parse
  - SecurityContext không có Authentication
  - Controller lấy được "anonymousUser" → lỗi

**Q11: SecurityContext lưu gì?**
- **Trả lời:** Lưu Authentication object của request hiện tại, chứa:
  - Principal (UserDetails)
  - Authorities (roles/permissions)
  - Authenticated flag

**Q12: Làm sao lấy username từ SecurityContext trong Controller?**
- **Trả lời:**
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName(); // hoặc
UserDetails userDetails = (UserDetails) auth.getPrincipal();
String username = userDetails.getUsername();
```

---

### **PHẦN 4: FLOW & API**

**Q13: Register và Login khác nhau như thế nào?**
- **Trả lời:**
  - **Register:** Tạo user mới, hash password, lưu DB, không tạo token
  - **Login:** Verify password, tạo JWT token, trả về token + user info

**Q14: Tại sao Get Me cần token?**
- **Trả lời:** 
  - Là protected endpoint
  - Cần token để xác định user nào đang request
  - Token được parse trong JwtAuthenticationFilter

**Q15: Nếu token sai hoặc hết hạn thì sao?**
- **Trả lời:**
  - JwtAuthenticationFilter catch exception, không set Authentication
  - SecurityContext trống
  - Controller throw "Chưa đăng nhập" hoặc Spring Security trả về 401 Unauthorized

---

### **PHẦN 5: VALIDATION & ERROR HANDLING**

**Q16: @Valid annotation làm gì?**
- **Trả lời:** Kích hoạt validation trên DTO. Nếu validation fail, throw `MethodArgumentNotValidException`.

**Q17: Validation error được xử lý ở đâu?**
- **Trả lời:** GlobalExceptionHandler bắt `MethodArgumentNotValidException` và format response với danh sách lỗi.

**Q18: Các annotation validation phổ biến?**
- **Trả lời:**
  - `@NotBlank`: Không được để trống (String)
  - `@NotNull`: Không được null
  - `@Email`: Phải là email hợp lệ
  - `@Size(min, max)`: Độ dài trong khoảng
  - `@Pattern(regexp)`: Phải match regex

---

## 🎯 CHECKLIST KIẾN THỨC

Sau ngày 3, bạn PHẢI hiểu và trả lời được:

- [ ] Spring Security cơ bản: SecurityFilterChain, PasswordEncoder, AuthenticationManager
- [ ] JWT: Cấu trúc, cách tạo/parse/validate token
- [ ] Các component: JwtUtils, CustomUserDetailsService, JwtAuthenticationFilter
- [ ] Flow: Register, Login, Get Me (có token)
- [ ] SecurityContext: Cách lấy username từ SecurityContext
- [ ] Validation: @Valid, các annotation, error handling
- [ ] Tại sao cần add filter vào SecurityConfig
- [ ] Tại sao token hết hạn thì phải login lại
- [ ] Cách test API với Postman (Bearer Token)

---

## 📝 GHI CHÚ QUAN TRỌNG

1. **JWT là stateless:** Server không lưu session, mọi thông tin nằm trong token
2. **Filter phải được add vào SecurityConfig:** Nếu không, token không được xử lý
3. **Password phải hash trước khi lưu DB:** Không bao giờ lưu plain text
4. **Token hết hạn phải login lại:** Hoặc implement refresh token (nâng cao)
5. **Validation ở DTO:** Dùng @Valid + annotations, không validate trong Service

---

## 🔗 TÀI LIỆU THAM KHẢO

- Spring Security Documentation: https://docs.spring.io/spring-security/reference/
- JWT.io: https://jwt.io/ (để decode/encode token test)
- BCrypt: https://en.wikipedia.org/wiki/Bcrypt








