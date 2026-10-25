# Phase 1.2 — Module Boundaries & Dependency Rules

Tài liệu này xác định ranh giới nghiệp vụ độc lập (Bounded Contexts), quy tắc phân tầng phụ thuộc một chiều (DAG) và ma trận giao tiếp giữa các module trong kiến trúc **Modular Monolith** của E-Edu.

---

## 1. Mục Tiêu & Nguyên Tắc Kiến Trúc

> **Nguyên tắc ranh giới bất biến:**  
> Mỗi module nghiệp vụ sở hữu toàn quyền đối với dữ liệu (schema riêng) và quy tắc nghiệp vụ của mình. Tuyệt đối không có module nào được phép truy vấn trực tiếp bảng hoặc gọi JPA Entity/Repository nội bộ của module khác.

1. **Explicit Dependencies (Phụ thuộc có hướng rõ ràng)**: Mọi phụ thuộc giữa các module phải tuân theo đồ thị có hướng không chu trình (DAG).
2. **Loosely Coupled Communication (Giao tiếp lỏng)**: Sử dụng Java Interface công khai (`public facade`) cho đồng bộ và `Domain Events` cho bất đồng bộ.
3. **Database Consistency mà không vi phạm ranh giới**: Dùng chung một cơ sở dữ liệu PostgreSQL nhưng phân tách rõ rệt thành các schema độc lập theo từng module.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Bounded Contexts & Modules](01-bounded-contexts-and-modules.md) | Phân chia 11 module nghiệp vụ thành 3 nhóm: Platform Modules, Education Core và Academic Operations. |
| **02** | [Dependency Graph & Rules](02-dependency-graph-and-rules.md) | Đồ thị phụ thuộc (Mermaid DAG), quy tắc cấm cyclic dependencies và cơ chế bảo vệ kiến trúc. |
| **03** | [Communication Patterns & Events](03-communication-patterns-and-events.md) | Đặc tả 2 cơ chế giao tiếp chính: Synchronous Public APIs (Query/Command) và Asynchronous Domain Events. |
| **04** | [Anti-Corruption & Data Isolation](04-anti-corruption-and-isolation.md) | Nguyên tắc cô lập dữ liệu: cấm Physical Cross-Schema Foreign Keys, kỹ thuật Logical UUID References và DTO Mapping. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 1.2

- [x] Phân chia ranh giới rõ ràng cho 11 module nghiệp vụ và 2 module kỹ thuật (`boot`, `shared-kernel`).
- [x] Thiết lập đồ thị phụ thuộc một chiều DAG, xác nhận không có bất kỳ phụ thuộc vòng (Cyclic Dependency) nào.
- [x] Chuẩn hóa hợp đồng giao tiếp giữa các module: Public Port/Facade và Domain Events.
- [x] Cấm truy cập chéo database table/entity; chỉ tham chiếu chéo qua Logical ID (`UUID`).
- [x] Tự động hóa kiểm tra các quy tắc kiến trúc này bằng ArchUnit trong Phase 2.

---

> Chuyển tiếp sang: [Phase 1.3 — Core Architecture Decisions (ADRs)](../phase1.3/README.md)
