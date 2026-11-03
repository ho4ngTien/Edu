# Phase 4.7 — Dynamic RBAC Permission Catalog & Resource-Level Authorization

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho hệ thống phân quyền nâng cao: **Fine-Grained RBAC** (Role-Based Access Control) với Permission Catalog động, Method-Level Security và Resource-Level Authorization trong kiến trúc **Defense-in-Depth 4 tầng**.

---

## 1. Mục tiêu & Mô hình Phân quyền

Hệ thống phân quyền E-Edu áp dụng kiến trúc **4 tầng kiểm soát (Four-Tier Defense-in-Depth)**:

| Tầng | Tên | Cơ chế | Thực thể phụ trách |
| :---: | :--- | :--- | :--- |
| **Tier 1** | Identity & Authentication | Xác minh JWT Access Token | Spring Security OAuth2 Resource Server |
| **Tier 2** | Tenant Context Resolution | Xác thực `X-Organization-Id` + Membership | `TenantResolutionFilter` |
| **Tier 3** | Method-Level Authorization | `@PreAuthorize("hasAuthority('...')")` | Spring Security Method Security |
| **Tier 4** | Resource-Level Authorization | Kiểm tra quyền trên từng thực thể cụ thể | Module sở hữu tài nguyên |

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-4.7.1** | **Permission Model & Canonical Catalog** | Permission có cấu trúc `DOMAIN_RESOURCE_ACTION` (ví dụ: `ORGANIZATION_MEMBER_INVITE`, `ASSESSMENT_GRADE_WRITE`). Phân biệt System Permission và Organization-scoped Permission. |
| **AC-4.7.2** | **Flyway Migration V6 (Roles & Permissions Schema)** | Migration `V6__create_organization_roles_and_permissions.sql` tạo 4 bảng: `organization.roles`, `organization.permissions`, `organization.role_permissions`, `organization.membership_roles`. |
| **AC-4.7.3** | **Dynamic Permission Loading** | Tại thời điểm xác thực JWT, hệ thống tra cứu danh sách quyền từ DB và nạp vào `SecurityContext` dưới dạng `GrantedAuthority`. Quyền phản ánh tức thì khi thay đổi trong DB. |
| **AC-4.7.4** | **Method-Level Security (`@PreAuthorize`)** | `@EnableMethodSecurity` được kích hoạt. Mọi service method quan trọng đều có `@PreAuthorize("hasAuthority('PERMISSION_CODE')")`. |
| **AC-4.7.5** | **Resource-Level Authorization (Anti-IDOR)** | Module sở hữu dữ liệu tự thẩm định quyền truy cập thực thể: luôn filter `WHERE organization_id = :orgId` ngay trong SQL query, không bao giờ tải dữ liệu rồi lọc bằng Java. |
| **AC-4.7.6** | **Anti-Information Leakage (404 vs 403)** | Khi user truy cập tài nguyên thuộc tenant khác, hệ thống trả 404 Not Found thay vì 403 Forbidden để tránh tiết lộ sự tồn tại của tài nguyên. |
| **AC-4.7.7** | **Integration Test Matrix (12 Test Cases)** | Bao gồm test cases: OWNER, ADMIN, MEMBER quyền khác nhau; cross-org access bị từ chối; suspended membership bị từ chối. |

---

## 3. Danh mục Quyền Chuẩn hóa (Permission Catalog)

### Quy ước đặt tên: `DOMAIN_RESOURCE_ACTION`

```text
DOMAIN: ORGANIZATION, IDENTITY, PEOPLE, ACADEMIC, CURRICULUM, ENROLLMENT, TEACHING, ATTENDANCE, ASSESSMENT, CERTIFICATION
RESOURCE: MEMBER, USER, STUDENT, TEACHER, CLASS, SUBJECT, ENROLLMENT, SCHEDULE, ATTENDANCE, GRADE, CERTIFICATION
ACTION: READ, WRITE, DELETE, INVITE, REVOKE, EVALUATE
```

### Danh mục Quyền theo Module

| Mã Quyền | Phạm vi | Mô tả | Vai trò mặc định |
| :--- | :--- | :--- | :--- |
| `ORGANIZATION_MEMBER_INVITE` | Organization | Mời thành viên mới | OWNER, ADMIN |
| `ORGANIZATION_MEMBER_REVOKE` | Organization | Thu hồi tư cách thành viên | OWNER, ADMIN |
| `ORGANIZATION_SETTINGS_WRITE` | Organization | Chỉnh sửa thông tin tổ chức | OWNER |
| `PEOPLE_STUDENT_WRITE` | Organization | Thêm/chỉnh sửa hồ sơ học sinh | OWNER, ADMIN |
| `PEOPLE_TEACHER_WRITE` | Organization | Thêm/chỉnh sửa hồ sơ giáo viên | OWNER, ADMIN |
| `ACADEMIC_CLASS_WRITE` | Organization | Tạo/chỉnh sửa lớp học | OWNER, ADMIN |
| `ATTENDANCE_RECORD_WRITE` | Organization | Nhập điểm danh | OWNER, ADMIN, MEMBER (giáo viên) |
| `ASSESSMENT_GRADE_WRITE` | Organization | Nhập điểm | OWNER, ADMIN, MEMBER (giáo viên phụ trách) |
| `ASSESSMENT_GRADE_READ` | Organization | Xem điểm | OWNER, ADMIN, MEMBER |
| `CERTIFICATION_EVALUATE` | Organization | Đánh giá đủ điều kiện cấp chứng chỉ | OWNER, ADMIN |

---

## 4. Thiết kế Database Schema (Flyway V6)

### `V6__create_organization_roles_and_permissions.sql`

```sql
-- Bảng danh mục quyền hạn
CREATE TABLE IF NOT EXISTS organization.permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    scope VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION',
    CONSTRAINT uk_permissions_code UNIQUE (code),
    CONSTRAINT ck_permissions_scope CHECK (scope IN ('SYSTEM', 'ORGANIZATION'))
);

-- Bảng vai trò theo tổ chức
CREATE TABLE IF NOT EXISTS organization.roles (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_roles_organization FOREIGN KEY (organization_id)
        REFERENCES organization.organizations (id),
    CONSTRAINT uk_roles_org_name UNIQUE (organization_id, name)
);

-- Bảng phân quyền theo vai trò
CREATE TABLE IF NOT EXISTS organization.role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES organization.roles (id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES organization.permissions (id)
);

-- Bảng phân công vai trò cho membership
CREATE TABLE IF NOT EXISTS organization.membership_roles (
    membership_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (membership_id, role_id),
    CONSTRAINT fk_mr_membership FOREIGN KEY (membership_id)
        REFERENCES organization.organization_memberships (id),
    CONSTRAINT fk_mr_role FOREIGN KEY (role_id) REFERENCES organization.roles (id)
);

-- Seed data: Permission catalog
INSERT INTO organization.permissions (id, code, description, scope) VALUES
    (gen_random_uuid(), 'ORGANIZATION_MEMBER_INVITE', 'Mời thành viên mới vào tổ chức', 'ORGANIZATION'),
    (gen_random_uuid(), 'ORGANIZATION_MEMBER_REVOKE', 'Thu hồi tư cách thành viên', 'ORGANIZATION'),
    (gen_random_uuid(), 'PEOPLE_STUDENT_WRITE', 'Thêm và chỉnh sửa hồ sơ học sinh', 'ORGANIZATION'),
    (gen_random_uuid(), 'ASSESSMENT_GRADE_WRITE', 'Nhập điểm kiểm tra', 'ORGANIZATION'),
    (gen_random_uuid(), 'ASSESSMENT_GRADE_READ', 'Xem điểm kiểm tra', 'ORGANIZATION'),
    (gen_random_uuid(), 'CERTIFICATION_EVALUATE', 'Đánh giá điều kiện cấp chứng chỉ', 'ORGANIZATION');
```

---

## 5. Method-Level Security

```java
// Kích hoạt Method Security trong Boot module
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
}

// Ví dụ sử dụng @PreAuthorize trong Service
@Service
public class GradeService {

    @PreAuthorize("hasAuthority('ASSESSMENT_GRADE_WRITE')")
    @Transactional
    public GradeView submitGrade(SubmitGradeCommand command) {
        OrganizationId orgId = TenantContext.getOrganizationId();
        // Resource-Level Check: giáo viên chỉ được nhập điểm môn mình phụ trách
        validateTeacherOwnership(command.teacherId(), command.subjectId(), orgId);
        // ... business logic
    }

    @PreAuthorize("hasAuthority('ASSESSMENT_GRADE_READ')")
    @Transactional(readOnly = true)
    public List<GradeView> getGradesByStudent(UUID studentId) {
        OrganizationId orgId = TenantContext.getOrganizationId();
        // Luôn filter by organizationId ngay trong SQL — không bao giờ filter bằng Java sau khi load
        return gradeRepository.findAllByStudentIdAndOrganizationId(studentId, orgId.value())
                .stream().map(this::mapToView).toList();
    }
}
```

---

## 6. Chính sách Lỗi Phân quyền (Anti-Information-Leakage Policy)

```java
// Khi truy cập tài nguyên không thuộc tenant → trả 404, không phải 403
@Transactional(readOnly = true)
public StudentView getStudent(UUID studentId) {
    OrganizationId orgId = TenantContext.getOrganizationId();

    // Query luôn có điều kiện organizationId → resource thuộc tenant khác sẽ không tìm thấy
    return studentRepository.findByIdAndOrganizationId(studentId, orgId.value())
            .map(this::mapToView)
            .orElseThrow(() -> new ResourceNotFoundException(
                    ErrorCode.STUDENT_NOT_FOUND,
                    "Student not found with id: " + studentId
            ));
    // Trả 404 Not Found — không để lộ rằng student có tồn tại ở tổ chức khác
}
```

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 4 README](README.md) — Tổng quan phân hệ Identity & Access
- [Phase 4.6: Tenant Resolution Context](phase4.6.md) — TenantResolutionFilter và TenantContext
- [Phase 4.5: Authentication Endpoints](phase4.5.md) — Login / Refresh / Logout
- [Phase 7.2: Architecture Tests với ArchUnit](../phase-7/phase7.2.md) — Kiểm soát ranh giới module
