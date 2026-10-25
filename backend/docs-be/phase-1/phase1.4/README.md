# Phase 1.4 — Module Structure & Layering

Tài liệu này đặc tả cấu trúc mã nguồn Maven Multi-Module, kiến trúc phân tầng nội bộ (Clean / Hexagonal Architecture) cho từng module, và quy chuẩn kiểm soát phạm vi truy cập (Package Visibility) trong E-Edu.

---

## 1. Mục Tiêu Thiết Kế Cấu Trúc

1. **Chuẩn hóa cấu trúc thư mục**: 100% module nghiệp vụ tuân thủ cùng một cấu trúc phân tầng nhất quán.
2. **Bảo vệ lõi nghiệp vụ (Domain Independence)**: Tầng domain và application không bị phụ thuộc vào framework, cơ sở dữ liệu hoặc thư viện bên ngoài.
3. **Kiểm soát ranh giới truy cập (Encapsulation)**: Chỉ mở các interface hợp đồng cần thiết ra ngoài package `api`; giấu kín toàn bộ mã nguồn triển khai bên trong `application`, `domain` và `infrastructure`.

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Maven Reactor & Packaging](01-maven-reactor-and-packaging.md) | Cây thư mục Maven Multi-Module, vai trò của Parent POM, Aggregator POM, Leaf Module POM và Boot POM. |
| **02** | [Clean / Hexagonal Layering](02-clean-hexagonal-layering.md) | Kiến trúc 4 tầng nội bộ (`api`, `application`, `domain`, `infrastructure`) và nguyên tắc đảo ngược phụ thuộc (DIP). |
| **03** | [Package Visibility & Public API](03-package-visibility-and-public-api.md) | Quy định phạm vi truy cập: `public` cho `api/`, `package-private` cho các tầng triển khai nội bộ, chống rò rỉ JPA Entity. |
| **04** | [Module Responsibilities & Schemas](04-module-responsibilities-and-schemas.md) | Bảng phân định trách nhiệm chi tiết, ánh xạ database schema và cấu trúc package của từng module trong 11 module nghiệp vụ. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 1.4

- [x] Định hình cấu trúc cây dự án Maven Multi-Module gồm 19 modules.
- [x] Phân chia ranh giới 4 tầng Clean Architecture nội bộ trong mỗi module nghiệp vụ.
- [x] Thiết lập nguyên tắc package visibility: bảo vệ `infrastructure` và `domain` không bị lộ ra ngoài.
- [x] Ánh xạ đầy đủ danh mục bảng, schema và package cho toàn bộ 11 module nghiệp vụ.

---

> Chuyển tiếp sang: [Phase 1.5 — Persistence & Database Standards](../phase1.5/README.md)
