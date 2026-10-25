# Phase 1.7 — Security Architecture & Authorization Model

Tài liệu này đặc tả toàn diện kiến trúc bảo mật không trạng thái (**Stateless Security**), mô hình phân quyền theo phạm vi tổ chức (**Organization-Scoped RBAC**), quy trình xử lý của Spring Security Filter Chain và cơ chế phòng chống tấn công IDOR trong E-Edu.

---

## 1. Mục Tiêu & Nguyên Tắc Bảo Mật

> **Bốn câu hỏi kiểm tra bắt buộc trong mọi request:**  
> 1. **Ai** đang thao tác? (Authenticated Principal / User ID)  
> 2. Thao tác trong **tổ chức** nào? (Tenant / Organization Scope)  
> 3. Có **vai trò & quyền hạn** gì trong tổ chức đó? (Role / Permissions)  
> 4. Có quyền trên **bản ghi cụ thể** này hay không? (Resource-Level Authorization)

- **Stateless Authentication**: Sử dụng JWT Access Token và Opaque Refresh Token, không lưu trữ server session.
- **Tenant Isolation**: Ngăn chặn tuyệt đối việc người dùng Trường A đọc/sửa dữ liệu Trường B (phòng chống lỗ hổng IDOR).
- **Auditability**: Lưu vết toàn bộ các hành động nhạy cảm (cấp quyền, sửa điểm số, thay đổi quy tắc).

---

## 2. Danh Mục Tài Liệu Thành Phần Chi Tiết

| STT | Tài liệu | Mô tả chi tiết |
| :---: | :--- | :--- |
| **01** | [Security Principles & Threat Model](01-security-principles-and-threat-model.md) | Bốn trụ cột bảo mật, mô hình đe dọa (OWASP API Security), nguyên tắc đặc quyền tối thiểu (Least Privilege). |
| **02** | [Organization-Scoped RBAC](02-organization-scoped-rbac.md) | Tách bạch giữa quyền hệ thống và quyền tổ chức: `MembershipRole` thay vì gán role toàn cục; cấu trúc Role - Permission. |
| **03** | [Security Filter Chain & Pipeline](03-security-filter-chain-and-pipeline.md) | Kiến trúc bộ lọc Spring Security 6: trích xuất Bearer Token, kiểm tra chữ ký HS256, chuyển đổi lỗi 401/403 thành `ApiErrorResponse`. |
| **04** | [Tenant Context Resolution & IDOR Prevention](04-tenant-context-resolution.md) | Trích xuất header `X-Organization-Id`, nạp `TenantContext`, xác minh quyền thành viên qua Public Port của `organization`. |

---

## 3. Tiêu Chí Nghiệm Thu Hoàn Thành Phase 1.7

- [x] Thiết kế mô hình RBAC theo phạm vi tổ chức (Organization-Scoped RBAC).
- [x] Đặc tả luồng xử lý của Stateless Spring Security Filter Chain.
- [x] Xây dựng cơ chế trích xuất và bảo vệ Tenant Context tự động.
- [x] Chuẩn hóa ánh xạ lỗi 401 Unauthorized và 403 Forbidden về `ApiErrorResponse`.

---

> Chuyển tiếp sang: [Phase 1.8 — Application Bootstrap, Configuration & Observability](../phase1.8/README.md)
