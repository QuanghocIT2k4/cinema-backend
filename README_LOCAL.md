# 🚀 HƯỚNG DẪN CHẠY LOCAL

## ✅ **CHỈ CẦN 3 BƯỚC:**

### **BƯỚC 1: Start MySQL**
```bash
docker-compose up -d
```

### **BƯỚC 2: Build Project**
```bash
mvn clean install
```

### **BƯỚC 3: Run Application**
```bash
mvn spring-boot:run
```

**HOẶC DÙNG SCRIPT TỰ ĐỘNG:**

**Windows:**
```bash
start-local.bat
```

**Linux/Mac:**
```bash
chmod +x start-local.sh
./start-local.sh
```

---

## 📋 **CẤU HÌNH MẶC ĐỊNH:**

- **Database:** MySQL 8.0 (Docker)
- **Port:** 3307 (host) → 3306 (container)
- **Database Name:** `cinema_db`
- **Username:** `root`
- **Password:** `rootpassword`
- **Backend Port:** `8080`

---

## 🔧 **KIỂM TRA:**

1. **MySQL đã chạy:**
   ```bash
   docker ps
   ```
   → Phải thấy container `cinema_mysql`

2. **Backend đã chạy:**
   → Mở browser: `http://localhost:8080/api/health`
   → Phải thấy: `{"status":"ok"}`

---

## 🛑 **DỪNG:**

```bash
docker-compose down
```

---

## ⚠️ **LƯU Ý:**

- **Local:** Dùng `application.properties` (MySQL port 3307)
- **Production:** Dùng `application-production.properties` (Railway tự động inject DATABASE_URL)
- **DatabaseConfig:** Chỉ override khi có env vars từ Railway, local dùng default

---

## 🐛 **TROUBLESHOOTING:**

**MySQL không start:**
```bash
docker-compose down
docker-compose up -d
docker-compose logs mysql
```

**Port 3307 đã được dùng:**
→ Sửa port trong `docker-compose.yml` và `application.properties`

**Backend không connect database:**
→ Kiểm tra MySQL đã ready: `docker-compose logs mysql`
→ Đợi 10-15 giây sau khi start MySQL

