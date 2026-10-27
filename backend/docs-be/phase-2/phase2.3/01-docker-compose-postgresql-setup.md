# Phase 2.3 — Thành phần 01: Docker Compose & PostgreSQL Setup

Tài liệu này đặc tả cấu hình container PostgreSQL chạy qua Docker Compose tại thư mục `backend/infrastructure/`.

---

## 1. Cấu Hình `docker-compose.yml`

Tệp đặt tại: `backend/infrastructure/docker-compose.yml`

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: edu-postgres
    restart: unless-stopped
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: edu
      POSTGRES_USER: edu_user
      POSTGRES_PASSWORD: edu_secret
      TZ: UTC
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U edu_user -d edu"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
    driver: local
```

---

## 2. Các Lệnh Vận Hành Hạ Tầng

Thực hiện từ thư mục `backend/infrastructure/`:

### Khởi động container nền:
```bash
docker compose up -d
```

### Kiểm tra trạng thái container và healthcheck:
```bash
docker compose ps
```

### Xem log khởi động của PostgreSQL:
```bash
docker compose logs -f postgres
```

### Dừng hạ tầng nhưng giữ lại dữ liệu:
```bash
docker compose down
```
