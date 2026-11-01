# Checkpoint 2.7.1 — Architecture Decisions & Authorization Model

> **Thuộc Phase**: [Phase 2.7 — Authorization & Access Control](README.md)  
> **Trạng thái**: 📐 Đã đặc tả kiến trúc (Architecture Approved)  
> **Tiêu chí nghiệm thu**: `AC-2.7.1`

---

## 1. Mục tiêu Checkpoint

Xác lập và chính thức hóa nền tảng kiến trúc phân quyền cho toàn bộ hệ thống E-Edu thông qua 4 Quyết định Kiến trúc (ADR-017 đến ADR-020). Mô hình phân quyền bảo đảm tính linh hoạt cao (Fine-Grained RBAC), khả năng cô lập đa tổ chức (Tenant Isolation), bảo vệ phân cấp từ endpoint đến service/resource, đồng thời tuân thủ triệt để nguyên tắc độc lập giữa các module trong Modular Monolith.

---

## 2. Các Quyết định Kiến trúc Cốt lõi (ADRs)

### 2.1. ADR-017: Fine-Grained RBAC & Dynamic Permission Catalog

- **Bối cảnh**: Hệ thống E-Edu có 12 module nghiệp vụ với hàng trăm thao tác từ quản lý tổ chức, tuyển sinh, tạo khóa học, chấm điểm đến cấp chứng chỉ. Nếu kiểm tra quyền chỉ dựa trên Role cố định (`hasRole('ADMIN')`, `hasRole('TEACHER')`), code nghiệp vụ sẽ bị coupling cứng nhắc với các vai trò, không thể hỗ trợ các tổ chức tự tạo Role tùy biến (Custom Roles) hoặc gán quyền hạn linh hoạt.
- **Quyết định**:
  1. Toàn bộ logic bảo vệ nghiệp vụ (`@PreAuthorize`) chỉ kiểm tra dựa trên **Permission nguyên tử** (`hasAuthority('PERMISSION_CODE')`).
  2. **Role** chỉ đóng vai trò là một "nhóm quyền" (Container of Permissions) để người quản trị dễ dàng gán cho người dùng.
  3. Danh mục quyền (Permission Catalog) được định nghĩa chuẩn hóa trong hệ thống theo định dạng `DOMAIN_RESOURCE_ACTION` (ví dụ: `ORG_MEMBER_MANAGE`, `COURSE_CREATE`, `GRADE_EDIT`).
  4. Các tổ chức được phép tạo Role mới và lựa chọn tập hợp các quyền trong danh mục cho phép.
- **Hệ quả**:
  - *Tích cực*: Tách rời hoàn toàn code nghiệp vụ khỏi tên vai trò; hỗ trợ doanh nghiệp tự định nghĩa chức danh nội bộ; mở rộng quyền hạn dễ dàng không cần refactor code.
  - *Thách thức*: Số lượng quyền nhiều đòi hỏi cơ chế cache quyền hiệu quả trong session/request để tránh query database liên tục.

---

### 2.2. ADR-018: Tenant-Scoped Role Assignment & System Role Separation

- **Bối cảnh**: E-Edu hoạt động theo mô hình Multi-Tenancy chia sẻ cơ sở dữ liệu (Shared Database, Separate Schemas). Một người dùng (User) có thể tham gia nhiều tổ chức khác nhau với các vai trò hoàn toàn khác nhau (ví dụ: là `TEACHER` ở Trường A nhưng là `STUDENT` ở Trung tâm B). Ngoài ra, còn có quản trị viên hệ sinh thái (System Admin) cần thực hiện các thao tác vận hành toàn hệ thống.
- **Quyết định**:
  1. Phân định rạch ròi giữa **System Roles** và **Organization Roles**:
     - **System Role**: Không gắn với bất kỳ `organization_id` nào (`organization_id IS NULL`), áp dụng cho các tác vụ nền tảng (quản lý server, giám sát hệ thống, tạo tổ chức mới).
     - **Organization Role**: Bắt buộc gắn liền với một `organization_id` cụ thể, chỉ có hiệu lực khi request chạy trong ngữ cảnh của tổ chức đó.
  2. Bảng liên kết quyền của thành viên (`organization.membership_roles`) ánh xạ giữa `membership_id` và `role_id`, đảm bảo quyền của người dùng tự động bị hủy bỏ nếu membership của họ bị đình chỉ hoặc chấm dứt.
  3. System Role không tự động vượt qua mọi rào cản tenant nếu không có chính sách đặc quyền và audit trail được ghi nhận rõ ràng.
- **Hệ quả**:
  - *Tích cực*: Triệt tiêu hoàn toàn rủi ro quyền hạn ở tổ chức này bị rò rỉ sang tổ chức khác; mô hình hóa chính xác quan hệ thực tế.
  - *Thách thức*: Đòi hỏi tầng Filter phải resolve chính xác Organization Context trước khi tải danh sách quyền của User.

---

### 2.3. ADR-019: Multi-Tier Authorization Pipeline (Four-Tier Defense)

- **Bối cảnh**: Các lỗ hổng bảo mật phổ biến như BOLA (Broken Object Level Authorization) và IDOR xảy ra khi việc kiểm tra quyền chỉ được thực hiện nửa vời ở API gateway hoặc tầng controller, trong khi tầng service và repository không kiểm tra quyền sở hữu tài nguyên.
- **Quyết định**: Xây dựng kiến trúc bảo vệ đa tầng nghiêm ngặt (Defense-in-Depth):
  1. **Tier 1 — Authentication**: Thẩm định JWT token, tính toàn vẹn chữ ký và thời hạn tại Spring Security Filter Chain.
  2. **Tier 2 — Tenant Context Resolution**: Trích xuất `X-Organization-Id`, xác minh membership hoạt động (`ACTIVE`) thông qua `OrganizationMembershipQuery` port, lưu trữ vào `TenantContext`.
  3. **Tier 3 — Method-Level Authorization**: Sử dụng Spring Security AOP Interceptor (`@PreAuthorize`) để chặn các request thiếu permission trước khi method nghiệp vụ được gọi.
  4. **Tier 4 — Resource-Level Authorization**: Module nghiệp vụ kiểm tra quyền sở hữu của tài nguyên (`resource.organizationId == tenantContext.organizationId`) và các điều kiện phân quyền chuyên biệt (ví dụ: giảng viên được phân công giảng dạy lớp).
- **Hệ quả**: Nếu một tầng kiểm soát bị bỏ sót hoặc lỗi cấu hình, các tầng còn lại vẫn bảo vệ dữ liệu an toàn.

---

### 2.4. ADR-020: Decoupled Resource Policy Ownership in Modular Monolith

- **Bối cảnh**: Trong hệ thống Modular Monolith, việc tập trung toàn bộ logic kiểm tra quyền của 12 module vào một service bảo mật duy nhất (`GlobalAuthorizationService`) sẽ gây ra sự phụ thuộc chéo nghiêm trọng (vi phạm Module Boundaries), khiến module bảo mật phải biết cấu trúc thực thể của tất cả các module khác.
- **Quyết định**:
  1. Phân quyền cấp tài nguyên thuộc quyền sở hữu của chính module quản lý tài nguyên đó.
  2. Mỗi module tự xây dựng service kiểm tra quyền nội bộ (ví dụ: `CourseAuthorizationService` trong module `curriculum`, `GradeAuthorizationService` trong module `assessment`).
  3. Tầng repository của mỗi module có trách nhiệm bổ sung điều kiện lọc tenant (`WHERE organization_id = :orgId`) ngay trong các câu lệnh SQL/JPA, không tải dữ liệu lên RAM rồi mới lọc.
- **Hệ quả**: Tuân thủ triệt để nguyên tắc độc lập module, dễ dàng tách thành microservices trong tương lai nếu cần.

---

## 3. Ma trận Phân chia Trách nhiệm Kiến trúc

| Tầng kiến trúc | Thành phần chịu trách nhiệm | Nhiệm vụ chính |
| :--- | :--- | :--- |
| **Edge / Web Filter** | `JwtSecurityConfig`, `TenantResolutionFilter` | Xác thực JWT, resolve `X-Organization-Id`, load danh sách quyền vào `SecurityContextHolder`. |
| **Cross-Module SPI** | `OrganizationMembershipQuery` (Port & Adapter) | Cung cấp thông tin membership và role/permission cho `TenantResolutionFilter` mà không để lộ thực thể database. |
| **Service Layer Proxy** | Spring Security `@PreAuthorize` Interceptor | Thẩm định quyền `hasAuthority(...)` trước khi thực thi service method. |
| **Business Logic** | Application Services (`*Service`) | Điều phối nghiệp vụ, nạp thực thể và gọi resource authorization service. |
| **Domain Resource Policy** | `*AuthorizationService` nội bộ từng module | Kiểm tra điều kiện nghiệp vụ sâu (vai trò giảng dạy, quyền duyệt hồ sơ, quyền xem điểm thi). |
| **Persistence / Data** | Spring Data JPA Repositories | Ép buộc lọc dữ liệu theo `organization_id` trong mọi truy vấn SQL. |

---

## 4. Tiêu chí Đánh giá & Nghiệm thu (Checklist AC-2.7.1)

- [x] **AC-2.7.1.1**: ADR-017 đến ADR-020 được văn bản hóa đầy đủ và lưu trữ trong cây thư mục tài liệu dự án.
- [x] **AC-2.7.1.2**: Mô hình phân quyền xác định rõ việc dựa vào Permission nguyên tử thay vì Role tĩnh trong code nghiệp vụ.
- [x] **AC-2.7.1.3**: Phân biệt rành mạch giữa System Roles và Organization-Scoped Roles.
- [x] **AC-2.7.1.4**: Quy định rõ cơ chế phòng thủ 4 tầng và trách nhiệm của từng tầng trong Modular Monolith.
- [x] **AC-2.7.1.5**: Module Boundaries được bảo toàn tuyệt đối, không có service toàn cục ôm đồm quy tắc của mọi module.
