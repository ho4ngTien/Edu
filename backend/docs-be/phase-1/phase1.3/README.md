# Phase 1.3 — Core Architecture Decisions (ADRs)

Tài liệu này tổng hợp các quyết định kiến trúc then chốt (Architecture Decision Records - ADRs) được phê duyệt cho toàn bộ hệ thống E-Edu, kèm theo bối cảnh, lý do lựa chọn và phân tích hệ quả kỹ thuật.

---

## 1. Tổng Hợp Các Quyết Định Kiến Trúc Được Phê Duyệt

| Mã ADR | Tiêu đề Quyết định | Trạng thái | Ngày phê duyệt | Tác động chính |
| :---: | :--- | :---: | :---: | :--- |
| **ADR-001** | [Shared Database Multi-Tenancy](01-adr-001-shared-database-multi-tenancy.md) | **Accepted** | 2026-10-25 | Dùng chung 1 PostgreSQL database, phân tách dữ liệu bằng `organization_id`. |
| **ADR-002** | [PostgreSQL Schema Per Module](02-adr-002-postgresql-schema-per-module.md) | **Accepted** | 2026-10-25 | Phân chia 12 PostgreSQL schemas độc lập tương ứng với từng module Bounded Context. |
| **ADR-003** | [Maven Multi-Module & ArchUnit](03-adr-003-maven-multi-module-and-archunit.md) | **Accepted** | 2026-10-25 | Kiểm soát ranh giới phụ thuộc thời gian biên dịch và kiểm thử tự động quy tắc kiến trúc. |
| **ADR-004** | [Generalized Rule Engine](04-adr-004-generalized-rule-engine.md) | **Accepted** | 2026-10-25 | Tách riêng module thẩm định quy tắc biểu thức nghiệp vụ thay vì hard-code trong domain. |
| **Analysis** | [Consequences & Mitigations](05-consequences-and-mitigations.md) | **Accepted** | 2026-10-25 | Ma trận đánh giá rủi ro, đánh đổi kỹ thuật (trade-offs) và giải pháp phòng ngừa. |

---

## 2. Tiêu Chuẩn Soạn Thảo ADR

Mỗi bản ghi quyết định kiến trúc (ADR) trong E-Edu tuân theo định dạng tiêu chuẩn Michael Nygard:
1. **Title**: Định danh và chủ đề quyết định.
2. **Status**: Trạng thái (`Proposed`, `Accepted`, `Deprecated`, `Superseded`).
3. **Context**: Vấn đề gặp phải, ràng buộc kỹ thuật và các lựa chọn thay thế được cân nhắc.
4. **Decision**: Quyết định được lựa chọn và nguyên tắc thực thi.
5. **Consequences**: Lợi ích mang lại, chi phí đánh đổi và biện pháp kiểm soát rủi ro.

---

> Chuyển tiếp sang: [Phase 1.4 — Module Structure & Layering](../phase1.4/README.md)
