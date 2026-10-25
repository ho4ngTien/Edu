# Phase 1.8 — Thành phần 04: Health Checks & Runtime Metrics

Tài liệu này đặc tả cấu hình Spring Boot Actuator, các đầu dò sức khỏe (Liveness & Readiness Probes) và thu thập chỉ số vận hành bằng Micrometer / Prometheus trong E-Edu.

---

## 1. Cấu Hình Spring Boot Actuator

Tích hợp starter `spring-boot-starter-actuator` vào `boot/pom.xml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
```

---

## 2. Các Đầu Dò Kiểm Tra Sức Khỏe (Health Probes)

Phục vụ việc điều phối container tự động trong Docker Compose và Kubernetes:

| Endpoint | Mục đích | Điều kiện đánh giá trạng thái UP |
| :--- | :--- | :--- |
| **`GET /actuator/health/liveness`** | Kiểm tra container có đang hoạt động hay bị treo luồng (Deadlock). Nếu DOWN, Kubernetes sẽ khởi động lại Pod. | Tiến trình JVM còn sống, thread chính không bị treo. |
| **`GET /actuator/health/readiness`**| Kiểm tra ứng dụng đã sẵn sàng tiếp nhận lưu lượng mạng (Traffic) hay chưa. Nếu DOWN, Router sẽ ngắt traffic. | Kết nối PostgreSQL DataSource thành công, Flyway migration hoàn tất. |

---

## 3. Thu Thập & Giám Sát Chỉ Số (Metrics)

Sử dụng Micrometer để đo lường các chỉ số trọng yếu:
- **`http.server.requests`**: Thời gian phản hồi (latency), số lượng request mỗi giây (throughput) và tỷ lệ mã lỗi HTTP (4xx, 5xx).
- **`hikaricp.connections`**: Số lượng kết nối database đang hoạt động (active), nhàn rỗi (idle) và thời gian chờ kết nối (connection timeout).
- **`jvm.memory.used`**: Dung lượng Heap Memory và Non-Heap Memory tiêu thụ.
- **Custom Business Metrics**: Số lượng tài khoản đăng ký mới mỗi ngày, số lượng giao dịch đăng ký học phần hoàn tất.
