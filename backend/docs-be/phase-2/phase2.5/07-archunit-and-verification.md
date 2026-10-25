# Phase 2.5: ArchUnit & Verification Guide

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.7. Kiểm soát Kiến trúc ArchUnit trong Phase 2.5

Module `tests/architecture-tests` mở rộng các kiểm thử ArchUnit để bảo đảm tính toàn vẹn của hai module `identity` và `organization`:

1. **AR-07 (`NoCrossSchemaDirectEntityAccess`)**: Không có class nào trong `com.edu.organization..` được phép phụ thuộc vào `com.edu.identity.infrastructure.persistence.entity..` hoặc `..repository..`.
2. **AR-08 (`PublicPortDependencyOnly`)**: Module `organization` chỉ giao tiếp với `identity` thông qua public ports (`com.edu.organization.application.port..`).
3. **AR-09 (`NoPasswordLeakageInDTOs`)**: Không một DTO nào trong `com.edu.identity.api.dto..` được phép chứa trường có tên `passwordHash` hoặc `rawPassword`.

---

## 5. Quy trình Kiểm thử & Xác minh (Verification Guide)

### Bước 1 — Khởi động Docker Database Container
Đảm bảo container `edu-postgres` hoạt động và nhận kết nối trên cổng 5432:

```powershell
docker ps
# Xác nhận edu-postgres ở trạng thái Up (healthy)
```

### Bước 2 — Thực thi Migrations & Kiểm tra Cấu trúc Bảng
Chạy migration qua ứng dụng hoặc công cụ Flyway:

```powershell
mvn -pl boot -am spring-boot:run
```

Kiểm tra trực tiếp database PostgreSQL:
```powershell
docker exec -it edu-postgres psql -U postgres -d edu -c "\dt identity.*"
docker exec -it edu-postgres psql -U postgres -d edu -c "\dt organization.*"
```

Kết quả mong đợi: Xuất hiện bảng `identity.users`, `organization.organizations`, `organization.organization_memberships`.

### Bước 3 — Chạy Toàn bộ Unit & Slice Tests cho Identity & Organization
```powershell
mvn -pl modules/identity test
mvn -pl modules/organization test
```

Xác nhận tất cả test nghiệp vụ và validation đạt 100% pass rate.

### Bước 4 — Kiểm thử Ranh giới Kiến trúc ArchUnit
```powershell
mvn -pl tests/architecture-tests -am test
```

Xác nhận không có vi phạm ranh giới module hoặc rò rỉ entity giữa `identity` và `organization`.

### Bước 5 — Xác minh Toàn diện Toàn bộ Reactor
```powershell
mvn clean verify
```

Kết quả mong đợi: `BUILD SUCCESS` trên toàn bộ 19 modules.

---

## 6. Checklist Nghiệm thu Phase 2.5

**Tiến độ Nghiệm thu Phase 2.5 (8/8 tiêu chí hoàn thành)**

| Tiêu chí | Mô tả | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.5.1** | Flyway migrations `V2`, `V3`, `V4` tạo bảng và indexes chuẩn xác | DONE |
| **AC-2.5.2** | JPA Entities (`UserEntity`, `OrganizationEntity`, `OrganizationMembershipEntity`) khớp schema validation | DONE |
| **AC-2.5.3** | `OrganizationService` & `MembershipService` triển khai đầy đủ use cases | DONE |
| **AC-2.5.4** | `IdentityService` thực hiện đăng ký và hash mật khẩu bằng BCrypt | DONE |
| **AC-2.5.5** | Public port `OrganizationAccountQuery` phân tách hoàn toàn ranh giới hai module | DONE |
| **AC-2.5.6** | Phân quyền RBAC (`OWNER`, `ADMIN`, `MEMBER`) và tenant scope được kiểm soát | DONE |
| **AC-2.5.7** | REST endpoints `/api/v1/identity/*` và `/api/v1/organizations/*` hoạt động đúng chuẩn | DONE |
| **AC-2.5.8** | `mvn clean verify` build thành công 19/19 module với đầy đủ test passes | DONE |

> [!NOTE]
> Checklist này phản ánh trạng thái nghiệm thu thực tế dựa trên kết quả build và kiểm thử mã nguồn, không tự động đánh dấu hoàn thành khi chưa có bằng chứng thực nghiệm trên repository.

---

## 7. Kế hoạch Chuyển giao sang Phase 2.6 (Security & JWT Token Management)

Sau khi Phase 2.5 hoàn tất nghiệm thu phần core data model và application services:

1. **Spring Security 6 Integration**:
   * Cấu hình `SecurityFilterChain` không trạng thái (stateless session).
   * Tạo `JwtTokenProvider` cấp phát Access Token (15 phút) và Refresh Token (7 ngày).
   * Triển khai bộ lọc `JwtAuthenticationFilter` phân tích token và nạp `SecurityContext`.

2. **Tenant Authentication & Login Flow**:
   * Endpoint `POST /api/v1/auth/login` xác thực credentials và trả về token kèm danh sách tổ chức người dùng thuộc về.
   * Header `X-Organization-Id` giải quyết tenant context cho từng request.

3. **Chuyển giao tới Education Core (Phase 3)**:
   * Bắt đầu xây dựng các module `people` (Person, Student, Teacher profiles) và `curriculum` dựa trên `organization_id`.
