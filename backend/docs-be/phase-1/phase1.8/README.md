# Phase 1.8 — Application Bootstrap, Configuration & Observability

Tài liệu này đặc tả quy chuẩn khởi động ứng dụng (**Composition Root Bootstrap**), quản lý cấu hình đa môi trường qua Spring Profiles, và chuẩn hóa hệ thống giám sát vận hành (**Observability**: Structured Logging, Correlation ID Tracing, Actuator Probes, Metrics) trong E-Edu.

---

## 1. Mục Tiêu Khởi Động & Giám Sát Vận Hành

1. **Điểm lắp ghép duy nhất (Composition Root)**: Mọi cấu hình framework, DataSource và Spring Security Filter Chain tập trung tại module `boot`; các module nghiệp vụ hoàn toàn độc lập và không phụ thuộc ngược lại vào `boot`.
2. **Quản lý cấu hình an toàn**: Tách biệt rõ ràng giữa cấu hình mặc định cho nhà phát triển máy local và cấu hình biến môi trường bảo mật cho môi trường sản xuất (`prod`).
3. **Giám sát vận hành toàn diện**: Mỗi request HTTP được gán một mã `Trace ID / Correlation ID` duy nhất; log có cấu trúc (Structured JSON) hỗ trợ tra cứu sự cố tức thì.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Composition Root Bootstrap](01-composition-root-bootstrap.md) | Vai trò của module `boot`, lớp `EduApplication.java`, nguyên tắc kiểm soát Component Scanning và nạp Bean có chủ đích. |
| **02** | [Multi-Environment Configuration](02-multi-environment-configuration.md) | Chiến lược quản lý cấu hình: `application.yml`, profile `local`, `test`, `prod`, bảo vệ secrets và cơ chế nạp biến môi trường. |
| **03** | [Observability: Logging & Tracing](03-observability-logging-and-tracing.md) | Structured logging với MDC, bộ lọc `TraceIdFilter`, truyền tải header `X-Trace-Id` và chuẩn hóa log kiểm toán an ninh. |
| **04** | [Health Checks & Metrics](04-health-checks-and-metrics.md) | Cấu hình Spring Boot Actuator, Liveness & Readiness Probes phục vụ Docker/K8s, tích hợp Micrometer và Prometheus metrics. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 1.8

- [x] Thiết lập module `boot` làm Composition Root duy nhất cho toàn bộ hệ thống.
- [x] Chuẩn hóa các profile cấu hình `local`, `test`, `prod` không để lộ mật khẩu trong git.
- [x] Cài đặt `TraceIdFilter` tự động gán mã truy vết Correlation ID cho 100% request.
- [x] Mở các endpoint Actuator health check cho Docker và Kubernetes probes.

---

> Xem thêm: [Backend Roadmap](../../backend-roadmap.md) & [Phase 2 — Project Setup & Core Domain Foundation](../../phase-2/README.md)
