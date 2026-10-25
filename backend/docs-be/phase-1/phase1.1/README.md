# Phase 1.1 — Domain Model & Entity Relationships

Tài liệu thiết kế mô hình dữ liệu cốt lõi cho hệ thống E-Edu theo kiến trúc **Modular Monolith**, hỗ trợ đa mô hình đào tạo (K-12, Đại học, Trung tâm Ngoại ngữ, Đào tạo nghề) mà không bị khóa chặt vào một mô hình cụ thể.

---

## 1. Mục tiêu và Nguyên tắc Thiết kế

> **Nguyên tắc cốt lõi:**  
> Mô hình hóa những khái niệm giáo dục dùng chung thành các thực thể (entity) độc lập, chuẩn hóa; các quy tắc đặc thù của từng mô hình giáo dục được quản lý thông qua cấu hình có kiểu và công cụ thẩm định quy tắc nghiệp vụ.

Hệ thống tách biệt rõ ràng 3 lớp khái niệm:
1. **Tài khoản và Tổ chức**: Định danh người dùng và đơn vị pháp nhân vận hành.
2. **Chương trình đào tạo**: Khung chương trình chuẩn và các môn học cấu thành.
3. **Tiến trình học tập thực tế**: Đợt mở lớp, đăng ký, chuyên cần và đánh giá kết quả.

---

## 2. Danh mục Tài liệu Thành phần Chi tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Domain Scope & Entities](01-domain-scope-and-entities.md) | Phân định 8 nhóm domain nghiệp vụ và danh mục thực thể cốt lõi trong toàn hệ thống. |
| **02** | [Organization → Program → Learning](02-organization-program-learning.md) | Phân cấp 3 tầng dữ liệu cốt lõi: Tổ chức, Chương trình đào tạo và Đợt triển khai giảng dạy thực tế. |
| **03** | [People & Identity Separation](03-people-and-identity-separation.md) | Tách bạch giữa tài khoản đăng nhập (`User`), hồ sơ con người (`Person`) và các vai trò nghiệp vụ (`Student`, `Teacher`, `Guardian`). |
| **04** | [Assessment & Grading Models](04-assessment-and-grading-models.md) | Mô hình đánh giá linh hoạt hỗ trợ điểm số phổ thông, tín chỉ đại học, band score ngoại ngữ và năng lực nghề nghiệp. |
| **05** | [Conceptual ERD & Entity Relationships](05-conceptual-erd-and-relationships.md) | Sơ đồ quan hệ thực thể mức khái niệm (Mermaid ERD) và quy tắc liên kết giữa các miền nghiệp vụ. |
| **06** | [Database Conventions & Multi-Tenancy](06-database-conventions-and-multi-tenancy.md) | Tiêu chuẩn định danh UUID, auditing, kiểm soát đa tổ chức (Multi-Tenancy) và ánh xạ sang Modular Monolith. |

---

## 3. Tiêu chí Nghiệm thu Hoàn thành Phase 1.1

- [x] Phân biệt rõ ràng giữa `Organization`, `Program`, `Course` và `CourseOffering`.
- [x] Tách bạch độc lập giữa `User`, `Person`, `Student`, `Teacher` và `Guardian`.
- [x] Tách riêng quá trình ghi danh chương trình (`ProgramEnrollment`) và đăng ký học phần (`CourseEnrollment`).
- [x] Thiết kế mô hình đánh giá (`AssessmentScheme`, `AssessmentResult`) hỗ trợ nhiều thang đo.
- [x] Xác định chiến lược cô lập dữ liệu tổ chức (`Tenant Isolation`) với `organization_id`.
- [x] Ánh xạ đầy đủ các thực thể vào 11 module nghiệp vụ trong Modular Monolith.

---

> Chuyển tiếp sang: [Phase 1.2 — Module Boundaries & Dependency Rules](../phase1.2/README.md)
