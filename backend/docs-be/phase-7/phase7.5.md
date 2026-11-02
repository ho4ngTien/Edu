# Phase 7.5 — CI/CD Pipeline & Containerization Deployment

Tài liệu đặc tả cấu hình **Docker multi-stage build**, **Docker Compose** cho production/staging, và **GitHub Actions CI/CD pipeline** cho backend E-Edu.

---

## 1. Mục tiêu & Phạm vi

- **Containerization**: Đóng gói ứng dụng thành Docker image tối ưu với multi-stage build (tách stage build Maven khỏi stage runtime JRE).
- **CI Pipeline**: GitHub Actions tự động chạy build, test (unit + architecture + integration) trên mỗi push/PR.
- **CD Pipeline**: Tự động build Docker image, push lên container registry, deploy lên staging environment khi merge vào `main`.
- **Security**: Quản lý secrets (JWT key, DB password) qua GitHub Secrets và Docker secrets — không hardcode trong code hay dockerfile.

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-7.5.1** | **Dockerfile Multi-Stage Build** | Stage 1: build với `maven:3.9-eclipse-temurin-21`. Stage 2: runtime với `eclipse-temurin:21-jre-alpine`. Image cuối cùng chỉ chứa JRE và JAR. |
| **AC-7.5.2** | **Docker Compose Production** | `docker-compose.prod.yml` định nghĩa services: `edu-app` và `edu-postgres` với health checks, volume persistence và restart policy. |
| **AC-7.5.3** | **GitHub Actions CI** | Workflow `ci.yml` chạy: checkout → setup Java 21 → cache Maven → `mvn verify` (unit + arch tests) → build Docker image. |
| **AC-7.5.4** | **Secrets Management** | `JWT_SECRET_BASE64`, `POSTGRES_PASSWORD` được inject qua GitHub Secrets → Docker env vars. Không có giá trị secrets nào trong git history. |
| **AC-7.5.5** | **Health Check Integration** | `docker-compose` sử dụng `healthcheck` dựa trên `/actuator/health` endpoint. `edu-app` chỉ start sau khi `edu-postgres` healthy. |

---

## 3. Dockerfile (Multi-Stage Build)

```dockerfile
# backend/Dockerfile

# ===== STAGE 1: Build =====
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies layer — chỉ copy pom.xml trước để tận dụng Docker layer cache
COPY pom.xml .
COPY boot/pom.xml boot/
COPY shared/shared-kernel/pom.xml shared/shared-kernel/
COPY modules/identity/pom.xml modules/identity/
COPY modules/organization/pom.xml modules/organization/
# ... (thêm các module pom.xml khác)

RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source và build
COPY . .
RUN mvn clean package -DskipTests -B --no-transfer-progress \
    -pl boot -am

# ===== STAGE 2: Runtime =====
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/boot/target/boot-*.jar app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

---

## 4. Docker Compose (Production/Staging)

```yaml
# backend/docker-compose.prod.yml
version: '3.9'

services:
  edu-postgres:
    image: postgres:15-alpine
    container_name: edu-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: edu_db
      POSTGRES_USER: edu_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - edu-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U edu_user -d edu_db"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    networks:
      - edu-network

  edu-app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: edu-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://edu-postgres:5432/edu_db
      SPRING_DATASOURCE_USERNAME: edu_user
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      EDU_SECURITY_JWT_SECRET_BASE64: ${JWT_SECRET_BASE64}
      EDU_SECURITY_JWT_ISSUER: e-edu-backend
      EDU_SECURITY_JWT_AUDIENCE: e-edu-clients
    depends_on:
      edu-postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - edu-network

volumes:
  edu-postgres-data:
    driver: local

networks:
  edu-network:
    driver: bridge
```

---

## 5. GitHub Actions CI/CD Workflow

```yaml
# .github/workflows/ci.yml
name: E-Edu Backend CI/CD

on:
  push:
    branches: [ main, develop ]
    paths:
      - 'backend/**'
  pull_request:
    branches: [ main, develop ]
    paths:
      - 'backend/**'

env:
  JAVA_VERSION: '21'
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}/edu-backend

jobs:
  build-and-test:
    name: Build & Test
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: backend

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build và chạy Unit + Architecture Tests
        run: mvn clean verify -B --no-transfer-progress

      - name: Chạy Integration Tests
        run: mvn verify -P integration-test -B --no-transfer-progress
        env:
          # Testcontainers cần Docker daemon
          TESTCONTAINERS_RYUK_DISABLED: true

      - name: Upload Test Reports
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: test-reports
          path: backend/**/target/surefire-reports/

  docker-build:
    name: Build Docker Image
    needs: build-and-test
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata for Docker
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=sha,prefix=sha-
            type=raw,value=latest,enable={{is_default_branch}}

      - name: Build và push Docker image
        uses: docker/build-push-action@v5
        with:
          context: backend
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

---

## 6. Biến Môi trường & Secrets

| Biến | Nơi cấu hình | Mô tả |
| :--- | :--- | :--- |
| `JWT_SECRET_BASE64` | GitHub Secrets | Khóa ký JWT HMAC-SHA256, tối thiểu 256-bit |
| `POSTGRES_PASSWORD` | GitHub Secrets | Mật khẩu PostgreSQL |
| `SPRING_PROFILES_ACTIVE` | docker-compose env | Profile `prod` cho production |

> [!CAUTION]
> Không bao giờ commit secrets vào git. Sử dụng `git-secrets` hoặc `.gitignore` để phòng ngừa. Tất cả secrets phải được rotate định kỳ (tối thiểu 90 ngày).

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 7 README](README.md) — Tổng quan phân hệ Quality & Delivery
- [Phase 7.3: Integration Tests](phase7.3.md) — Testcontainers trong CI
- [Phase 3.5: Actuator & Health Checks](../phase-3/phase3.5.md) — Health endpoint cho Docker healthcheck

