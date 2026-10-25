# Phase 1.6 — API Contracts & Cross-Module Communication

Tài liệu này đặc tả toàn diện các hợp đồng giao tiếp giữa các module nội bộ (Synchronous Public Ports / Facades, Asynchronous Domain Events, Transactional Outbox Pattern) và chuẩn hóa giao diện REST API hướng ngoại (REST API Contracts & Error Specifications) trong E-Edu.

---

## 1. Mục Tiêu Chuẩn Hóa Giao Tiếp

1. **Giao tiếp liên module lỏng (Loose Coupling)**: Các module giao tiếp qua Interface công khai hoặc Domain Events; không có module nào gọi trực tiếp JPA Entity hoặc Database table của module khác.
2. **Đảm bảo tính tin cậy của sự kiện (Event Reliability)**: Áp dụng Transactional Outbox Pattern để sự kiện nghiệp vụ không bị mất khi có sự cố server hoặc database rollback.
3. **Thống nhất trải nghiệm API (Consistent API Experience)**: 100% REST endpoints tuân theo cùng một định dạng JSON phản hồi (`ApiErrorResponse`, cấu trúc phân trang chuẩn, lọc dữ liệu an toàn).

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Synchronous Public Facades](01-synchronous-public-facades.md) | Giao tiếp đồng bộ qua Java Interfaces, Public Query / Command Ports, DTO bất biến (`Record`), nguyên tắc Dependency Inversion (DIP). |
| **02** | [Asynchronous Domain Events](02-asynchronous-domain-events.md) | Giao tiếp bất đồng bộ qua Spring Application Events, hợp đồng `DomainEvent`, quy tắc `@TransactionalEventListener(AFTER_COMMIT)`. |
| **03** | [Transactional Outbox Pattern](03-transactional-outbox-pattern.md) | Thiết kế bảng Outbox, đảm bảo tính nguyên tử (Atomicity) giữa thay đổi dữ liệu và phát sự kiện, cơ chế Idempotent Consumer. |
| **04** | [REST API Response & Error Contracts](04-rest-api-response-and-error-contracts.md) | Định dạng phản hồi thành công, cấu trúc lỗi thống nhất `ApiErrorResponse`, chuẩn phân trang và quy tắc HTTP Status Codes. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 1.6

- [x] Thiết lập chuẩn Interface công khai và Record DTOs trong package `api` của mỗi module.
- [x] Xây dựng cơ chế phát và lắng nghe Domain Events an toàn sau commit.
- [x] Đặc tả kiến trúc Transactional Outbox phòng chống mất mát dữ liệu.
- [x] Chuẩn hóa hợp đồng JSON cho toàn bộ REST API endpoints.

---

> Chuyển tiếp sang: [Phase 1.7 — Security & Authorization Model](../phase1.7/README.md)
