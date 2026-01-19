# HƯỚNG DẪN RESTART VÀ RESEED DATABASE

## ✅ ĐÃ SỬA XONG LOGIC SEED MOVIE!

**Thay đổi:** 
- Phân bổ release_date dựa trên thời điểm hiện tại
- 30% phim ENDED (đã kết thúc)
- **50% phim NOW_SHOWING** (đang chiếu) ← ĐỦ ĐỂ SEED SHOWTIME!
- 20% phim COMING_SOON (sắp chiếu)

---

## 🚀 CÁCH NHANH NHẤT:

### Bước 1: Xóa Database
Mở phpMyAdmin (`http://localhost:8080`), chạy SQL:

```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE booking_refreshments;
TRUNCATE TABLE tickets;
TRUNCATE TABLE bookings;
TRUNCATE TABLE showtimes;
TRUNCATE TABLE seats;
TRUNCATE TABLE rooms;
TRUNCATE TABLE cinemas;
TRUNCATE TABLE refreshments;
TRUNCATE TABLE movies;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
```

### Bước 2: Restart Backend
1. Stop backend: **Ctrl+C** trong terminal
2. Chạy lại: `.\run-be.bat`

### Bước 3: Kiểm tra log
Phải thấy:
```
=== BẮT ĐẦU SEED DATA ===
Đang seed Users...
Đã seed 28 users (3 Admin + 25 Customer)
Đang seed Movies...
Đã seed 100 movies (30 NOW_SHOWING + 50 COMING_SOON + 20 ENDED)
Đang seed Cinemas và Rooms...
Đã seed 3 cinemas và 10 rooms
Đang seed Showtimes...
Đã seed 30 showtimes  ← QUAN TRỌNG!
=== HOÀN THÀNH SEED DATA ===
```

---

## ✅ KẾT QUẢ MONG ĐỢI:

Sau khi seed xong, kiểm tra trong phpMyAdmin:
- users: **28**
- movies: **100** (có ít nhất 30 phim NOW_SHOWING)
- cinemas: **3**
- rooms: **10**
- seats: **1000+**
- **showtimes: 30** ← KHÔNG CÒN 0 NỮA!

---

## 🎯 SAU KHI SEED XONG:

Tiếp tục **TUẦN 2 - NGÀY 5: Frontend Auth Pages & Layout** 🚀
