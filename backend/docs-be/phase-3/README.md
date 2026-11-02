# Phase 3 — Foundation & Observability

Tài liệu kỹ thuật cho tầng nền tảng hỗ trợ (cross-cutting concerns): xử lý lỗi chuẩn hóa, validation đầu vào, quản lý schema đa tầng bằng Flyway, logging phân tán và giám sát hoạt động hệ thống.

---

## Danh mục Tài liệu Kỹ thuật

| Phase | Tài liệu | Trạng thái | Mô tả |
| :---: | :--- | :---: | :--- |
| **3.1** | [Global Exception Handling](phase3.1.md) | ✅ Hoàn thành | `GlobalExceptionHandler`, `ApiErrorResponse`, `BusinessException`, `ErrorCode` |
| **3.2** | [Request Validation](phase3.2.md) | ✅ Hoàn thành | Jakarta Bean Validation, `@Valid`, `fieldErrors` chuẩn hóa |
| **3.3** | [Flyway Migrations Đa Schema](phase3.3.md) | ✅ Hoàn thành | V1–V5 migrations, schema `identity` & `organization`, chiến lược Flyway + Hibernate validate |
| **3.4** | [Centralized Logging & Correlation ID](phase3.4.md) | 🚧 Đang triển khai | `TraceIdFilter`, MDC, `X-Trace-Id` header, Logstash JSON encoder |
| **3.5** | [Actuator, Metrics & Health Indicators](phase3.5.md) | 🚧 Đang triển khai | Prometheus metrics, Custom Multi-Schema Health Check, bảo vệ actuator endpoints |

---

## Quyết định Kiến trúc Chính

| Quyết định | Nội dung |
| :--- | :--- |
| **Single Error Contract** | Tất cả API đều trả lỗi theo cấu trúc `ApiErrorResponse` — không bao giờ trả raw exception hay stack trace |
| **Typed Error Codes** | `ErrorCode` enum định nghĩa toàn bộ mã lỗi — ngăn hard-coded string và dễ localize về sau |
| **Flyway-only DDL** | Hibernate `ddl-auto: validate` — Flyway là nguồn sự thật duy nhất cho database schema |
| **TraceId Propagation** | Mọi request đều có `traceId` trong MDC và `X-Trace-Id` response header để hỗ trợ debugging phân tán |

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md).
