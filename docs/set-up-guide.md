# Hướng dẫn Thiết lập Môi trường — EduSMS

Tài liệu này hướng dẫn từng bước thiết lập môi trường phát triển cục bộ cho dự án **EduSMS**.

---

## 1. Yêu cầu Công cụ

Đảm bảo máy đã cài đặt:

| Tool | Version | Kiểm tra |
|------|---------|---------|
| Java JDK | **21** | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Node.js | 20 LTS | `node -v` |
| Docker Desktop | Latest | `docker -v` |
| Git | Latest | `git -v` |

---

## 2. Khởi động môi trường Dev

### Bước 1: Clone & checkout

```bash
git clone https://github.com/ho4ngTien/Edu.git
cd Edu
git checkout develop
```

### Bước 2: Khởi động Database & Infrastructure

```bash
cd infrastructure
docker-compose up -d
```

Kiểm tra PostgreSQL đang chạy: `docker ps`

### Bước 3: Chạy Backend

```bash
cd backend
mvn spring-boot:run
```

Backend chạy tại: `http://localhost:8080`

### Bước 4: Chạy Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại: `http://localhost:5173`

---

## 3. Khởi động nhanh (Windows)

Thay vì mở nhiều terminal, dùng script có sẵn:

```bash
# Chạy toàn bộ bằng Docker
infrastructure/run-docker.bat
```

---

## 4. Troubleshooting

| Lỗi | Nguyên nhân | Cách fix |
|-----|------------|---------|
| `Port 8080 already in use` | Port bị chiếm | Kill process hoặc đổi port trong `application.properties` |
| `Connection refused` (DB) | Docker chưa chạy | `docker-compose up -d` |
| `java.lang.UnsupportedClassVersionError` | JDK < 21 | Cập nhật `JAVA_HOME` trỏ tới JDK 21 |
| `npm install` lỗi | Node version sai | Kiểm tra `node -v`, cần >= 20 |
