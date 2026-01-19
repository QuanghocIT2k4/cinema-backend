# HƯỚNG DẪN DEPLOY BACKEND LÊN RENDER

## Bước 1: Chuẩn bị GitHub Repository

1. **Push code lên GitHub** (nếu chưa có):
   ```bash
   cd cinema-backend
   git init
   git add .
   git commit -m "Initial commit - Ready for Render deployment"
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```

## Bước 2: Tạo tài khoản Render (nếu chưa có)

1. Truy cập: https://render.com
2. Đăng ký/Đăng nhập bằng GitHub account
3. Kết nối GitHub repository

## Bước 3: Tạo MySQL Database trên Render

1. Vào **Dashboard** → **New +** → **PostgreSQL** (hoặc MySQL nếu có)
   - **Name**: `cinema-db`
   - **Database**: `cinema_db`
   - **User**: Render tự tạo
   - **Region**: Chọn gần bạn nhất
   - **Plan**: Free (hoặc Starter nếu muốn tốt hơn)
2. **Lưu lại thông tin**:
   - `DB_HOST` (Internal Database URL)
   - `DB_PORT` (thường là 3306)
   - `DB_NAME` (tên database)
   - `DB_USERNAME` (username)
   - `DB_PASSWORD` (password)

## Bước 4: Deploy Web Service (Backend API)

1. Vào **Dashboard** → **New +** → **Web Service**
2. **Connect repository**: Chọn repo chứa `cinema-backend`
3. **Cấu hình**:
   - **Name**: `cinema-backend-api`
   - **Region**: Cùng region với database
   - **Branch**: `main` (hoặc branch bạn muốn deploy)
   - **Root Directory**: `cinema-backend` (nếu repo có nhiều folder)
   - **Language**: Chọn **Docker** (Render không có preset Java, dùng Docker)
   - **Build Command**: (Để trống - Dockerfile sẽ tự build)
   - **Start Command**: (Để trống - Dockerfile sẽ tự start)
   - **Plan**: Free (hoặc Starter)

4. **Environment Variables** (tab "Environment"):
   ```
   SPRING_PROFILES_ACTIVE=production
   
   # Database (PostgreSQL từ Render)
   DB_HOST=dpg-d5n4163e5dus73etcgtg-a
   DB_PORT=5432
   DB_NAME=cinema_db_pc0h
   DB_USERNAME=cinema_user
   DB_PASSWORD=NWAvD2XnZGXYRFtHoQi7FeDTJfLABSan
   
   # JWT
   JWT_SECRET=<tạo một secret ngẫu nhiên dài ít nhất 32 ký tự>
   JWT_EXPIRATION_MS=86400000
   
   # CORS (sẽ cập nhật sau khi có URL Vercel)
   ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
   ```

   **Lưu ý**: 
   - `ALLOWED_ORIGINS`: Thêm URL Vercel của bạn (sẽ có sau khi deploy FE)
   - `JWT_SECRET`: Tạo một chuỗi ngẫu nhiên (ví dụ: `openssl rand -base64 32`)

5. **Click "Create Web Service"**
6. Render sẽ tự động build và deploy
7. **Đợi deploy xong** (5-10 phút lần đầu)
8. **Lấy URL**: Render sẽ cho URL kiểu `https://cinema-backend-api.onrender.com`

## Bước 5: Seed Database (Chạy DataSeeder)

Sau khi backend deploy xong, bạn cần seed data:

1. **Option 1**: Gọi API seed (nếu có endpoint seed)
2. **Option 2**: SSH vào container và chạy seed script
3. **Option 3**: Tạo một endpoint tạm thời để trigger DataSeeder

**Hoặc**: Chạy seed local và export SQL, rồi import vào Render database.

## Bước 6: Test API

1. Mở URL Render: `https://cinema-backend-api.onrender.com`
2. Test endpoint: `https://cinema-backend-api.onrender.com/api/movies`
3. Nếu thấy JSON response → ✅ Backend đã chạy!

## Bước 7: Cập nhật Frontend (Vercel)

1. Vào **Vercel Dashboard** → **Project Settings** → **Environment Variables**
2. Thêm/Cập nhật:
   ```
   VITE_API_BASE_URL=https://cinema-backend-api.onrender.com
   ```
3. **Redeploy** FE để áp dụng biến môi trường mới

## Bước 8: Cập nhật CORS trên Render

1. Vào **Render Dashboard** → **cinema-backend-api** → **Environment**
2. Cập nhật `ALLOWED_ORIGINS` với URL Vercel của bạn:
   ```
   ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-custom-domain.com
   ```
3. **Manual Deploy** để áp dụng thay đổi

## Troubleshooting

### Lỗi: "Cannot connect to database"
- Kiểm tra `DB_HOST` dùng **Internal Database URL** (không phải Public URL)
- Kiểm tra `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` đúng chưa

### Lỗi: "Port already in use"
- Render tự động set `PORT` qua biến môi trường, không cần set thủ công

### Lỗi: "CORS error"
- Kiểm tra `ALLOWED_ORIGINS` có đúng URL Vercel chưa
- Đảm bảo không có trailing slash `/` ở cuối URL

### Backend chậm (Free tier)
- Render free tier sẽ "sleep" sau 15 phút không có request
- Request đầu tiên sau khi sleep sẽ mất ~30s để wake up
- Nếu cần performance tốt hơn → upgrade lên Starter plan

## Tài liệu tham khảo

- Render Docs: https://render.com/docs
- Spring Boot on Render: https://render.com/docs/deploy-spring-boot

