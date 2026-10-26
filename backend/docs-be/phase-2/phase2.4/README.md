# Phase 2.4 — Shared Kernel & Domain Foundation

Tài liệu này đặc tả việc xây dựng nền tảng kỹ thuật dùng chung (**Shared Kernel**) và các thành phần cốt lõi (**Domain Foundation**) cho toàn bộ 12 module nghiệp vụ của E-Edu: Strongly Typed IDs, Base Entity Auditing, Domain Events, Global Exception Handling và bộ quy tắc kiểm thử kiến trúc ArchUnit.

---

## 1. Mục Tiêu Kỹ Thuật

1. **Định Danh Có Kiểu Mạnh (Strongly Typed IDs)**: Bọc UUID nguyên thủy bằng các Java Records bất biến (`UserId`, `OrganizationId`) để ngăn ngừa lỗi truyền nhầm ID giữa các thực thể khác loại ở tầng service.
2. **Nền Tảng Persistence & Auditing**: Cung cấp `@MappedSuperclass` `AuditableEntity` tự động ghi nhận `createdAt` và `updatedAt` thông qua Spring Data JPA Auditing và `Clock` UTC.
3. **Hợp Đồng Sự Kiện Nghiệp Vụ (Domain Events)**: Chuẩn hóa hợp đồng `DomainEvent` bất biến, tích hợp cơ chế phát sự kiện sau khi commit transaction (`@TransactionalEventListener`).
4. **Xử Lý Lỗi Toàn Cầu Nhất Quán**: `GlobalExceptionHandler` bắt mọi ngoại lệ và đóng gói thành JSON `ApiErrorResponse` chuẩn, không làm lộ stack trace ra ngoài.
5. **Cưỡng Chế Kiến Trúc Tự Động Với ArchUnit**: Xây dựng 9 quy tắc kiểm tra ranh giới phụ thuộc và cô lập module trong module `tests/architecture-tests`.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Strongly Typed IDs & Primitives](01-strongly-typed-ids-and-primitives.md) | Thiết kế `UserId`, `OrganizationId` bằng Java Record, cơ chế validation không null và chuyển đổi chuỗi. |
| **02** | [AuditableEntity & Persistence Foundation](02-auditable-entity-and-persistence-foundation.md) | Lớp cha `@MappedSuperclass` `AuditableEntity`, cấu hình `@EnableJpaAuditing` và inject `Clock` UTC bean. |
| **03** | [Domain Events Contract & Publisher](03-domain-events-contract-and-publisher.md) | Interface `DomainEvent`, quy chuẩn truyền dữ liệu bất biến và cơ chế lắng nghe sự kiện sau commit transaction. |
| **04** | [Global Exception Handling & Error Response](04-global-exception-handling-and-api-error-response.md) | Đặc tả cấu trúc JSON `ApiErrorResponse`, xử lý validation `fieldErrors` và lớp ngoại lệ `BusinessException`. |
| **05** | [ArchUnit Rules & Architecture Tests](05-archunit-rules-and-architecture-tests.md) | Chi tiết 9 quy tắc kiểm tra kiến trúc: cấm cyclic dependencies, cô lập shared-kernel, bảo vệ internal package. |
| **06** | [Verification & Testing Guide](06-verification-and-testing-guide.md) | Hướng dẫn chạy kiểm thử reactor `mvn clean verify`, kiểm tra kết quả unit tests và architecture tests. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 2.4

- [x] Tạo `UserId`, `OrganizationId` trong `shared-kernel` kèm Unit Tests.
- [x] Tạo `AuditableEntity` và cấu hình JpaAuditing.
- [x] Tạo `ApiErrorResponse` và `GlobalExceptionHandler` kèm WebMvc slice tests.
- [x] Viết và thực thi thành công 9 bài test ArchUnit trong `tests/architecture-tests`.
- [x] Toàn bộ build reactor `mvn clean verify` đạt kết quả `BUILD SUCCESS`.

---

> Chuyển tiếp sang: [Phase 2.5 — Identity & Organization Core](../phase2.5/README.md)
