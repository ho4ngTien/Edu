# Phase 4.2 — Organizations & Organization Memberships

Tài liệu thiết kế kiến trúc, đặc tả kỹ thuật và tiêu chí nghiệm thu cho phân hệ quản lý Tổ chức (Đại học / Trường học / Trung tâm đào tạo) và Tư cách thành viên (Memberships) trong module `organization`.

---

## 1. Mục tiêu & Phạm vi

- **Mô hình Multi-Tenancy cô lập**: Mỗi tổ chức là một đơn vị thuê độc lập (`Tenant`), định danh bằng `OrganizationId` (UUID). Mọi dữ liệu phân hệ giáo dục và vận hành học vụ sau này đều gắn chặt với ngữ cảnh tổ chức này.
- **Tư cách thành viên (Organization Membership)**: Một người dùng (từ schema `identity`) có thể tham gia nhiều tổ chức với các vai trò độc lập (`OWNER`, `ADMIN`, `MEMBER`).
- **Liên kết phi khóa ngoại vật lý (Non-Physical Foreign Key)**: Bảng `organization_memberships` lưu `user_id` kiểu `UUID NOT NULL` mà **không tạo physical foreign key** trỏ sang bảng `identity.users`. Tính toàn vẹn logic được đảm bảo qua Application Public Port.
- **Vòng đời trạng thái**: Quản lý trạng thái hoạt động của tổ chức (`ACTIVE`, `SUSPENDED`, `ARCHIVED`) và trạng thái thành viên (`ACTIVE`, `SUSPENDED`).

---

## 2. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
| :---: | :--- | :--- |
| **AC-4.2.1** | **Organization Persistence Migration** | Flyway migration `V3__create_organizations.sql` tạo bảng `organization.organizations` với các trường `id`, `code`, `name`, `status`, `created_at`, `updated_at`. `code` là duy nhất toàn cục. |
| **AC-4.2.2** | **Membership Persistence Migration** | Flyway migration `V4__create_organization_memberships.sql` tạo bảng `organization.organization_memberships` với composite unique constraint `(organization_id, user_id)`. |
| **AC-4.2.3** | **Tạo Tổ chức & Gán Owner Tự động** | Khi tạo tổ chức mới, `OrganizationService` tự động khởi tạo bản ghi `OrganizationMembership` với vai trò `OWNER` cho người khởi tạo. |
| **AC-4.2.4** | **Quản lý Vòng đời Thành viên** | `MembershipService` hỗ trợ mời thành viên (`InviteMember`), cập nhật trạng thái (`ACTIVE` <-> `SUSPENDED`), và kiểm tra tính hợp lệ của người dùng qua Port trước khi tạo membership. |
| **AC-4.2.5** | **Cô lập Dữ liệu Tenant** | Không bao giờ truy vấn danh sách thành viên hoặc chi tiết tổ chức mà không có điều kiện `organization_id`. |
| **AC-4.2.6** | **REST API Đầy đủ & Chuẩn hóa** | Endpoints `/api/v1/organizations` và `/api/v1/organizations/{orgId}/members` hoạt động ổn định, bắt lỗi trùng mã tổ chức (409 Conflict) và không tìm thấy (404 Not Found). |

---

## 3. Thiết kế Cơ sở dữ liệu & Flyway Migrations

### 3.1. Bảng Tổ chức (`V3__create_organizations.sql`)

```sql
CREATE TABLE IF NOT EXISTS organization.organizations (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_organizations_code UNIQUE (code),
    CONSTRAINT ck_organizations_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_organizations_status ON organization.organizations (status);
```

### 3.2. Bảng Tư cách Thành viên (`V4__create_organization_memberships.sql`)

```sql
CREATE TABLE IF NOT EXISTS organization.organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_memberships_organization FOREIGN KEY (organization_id) 
        REFERENCES organization.organizations (id) ON DELETE RESTRICT,
    CONSTRAINT uk_memberships_org_user UNIQUE (organization_id, user_id),
    CONSTRAINT ck_memberships_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
    CONSTRAINT ck_memberships_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

CREATE INDEX IF NOT EXISTS idx_memberships_org_id ON organization.organization_memberships (organization_id);
CREATE INDEX IF NOT EXISTS idx_memberships_user_id ON organization.organization_memberships (user_id);
```

> [!NOTE]
> Bảng `organization_memberships` chỉ có foreign key nội bộ `fk_memberships_organization` đến `organization.organizations`. Tuyệt đối không có foreign key trỏ sang `identity.users` để bảo vệ ranh giới database schema tự chủ.

---

## 4. Triển khai JPA Entities

### 4.1. `OrganizationEntity.java`

```java
@Entity
@Table(name = "organizations", schema = "organization")
public class OrganizationEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status;

    // Constructors, Getters & Business methods
}
```

### 4.2. `OrganizationMembershipEntity.java`

```java
@Entity
@Table(name = "organization_memberships", schema = "organization")
public class OrganizationMembershipEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private OrganizationRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MembershipStatus status;

    // Constructors, Getters & Business methods
}
```

---

## 5. Dịch vụ Nghiệp vụ (Application Services)

### 5.1. Tạo Tổ chức & Gán Quyền Sở hữu (`OrganizationService.java`)

```java
@Service
@Transactional
public class OrganizationService {

    private final OrganizationJpaRepository organizationRepository;
    private final OrganizationMembershipJpaRepository membershipRepository;
    private final OrganizationAccountQuery accountQuery;

    public OrganizationView createOrganization(CreateOrganizationCommand command) {
        if (organizationRepository.existsByCode(command.code())) {
            throw new DuplicateOrganizationCodeException(command.code());
        }

        // Xác thực người dùng khởi tạo tồn tại qua Public Port
        if (!accountQuery.existsActiveUser(command.ownerUserId())) {
            throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "Owner user not found or inactive");
        }

        UUID orgId = UUID.randomUUID();
        OrganizationEntity entity = new OrganizationEntity(
                orgId, command.code(), command.name(), OrganizationStatus.ACTIVE
        );
        organizationRepository.save(entity);

        // Tự động gán quyền OWNER
        OrganizationMembershipEntity ownerMembership = new OrganizationMembershipEntity(
                UUID.randomUUID(),
                orgId,
                command.ownerUserId().value(),
                OrganizationRole.OWNER,
                MembershipStatus.ACTIVE
        );
        membershipRepository.save(ownerMembership);

        return mapToView(entity);
    }
}
```

---

## 6. Hợp đồng REST API (Contract DTOs)

### Tạo tổ chức (`POST /api/v1/organizations`)
- **Request Body**:
```json
{
  "code": "BKU",
  "name": "Đại học Bách Khoa",
  "ownerUserId": "e9b2c3a4-1234-4567-89ab-cdef01234567"
}
```
- **Response 201 Created**:
```json
{
  "id": "7f1c9d8a-5678-4321-98ba-fe0123456789",
  "code": "BKU",
  "name": "Đại học Bách Khoa",
  "status": "ACTIVE",
  "createdAt": "2026-03-30T10:15:00Z"
}
```

### Mời thành viên vào tổ chức (`POST /api/v1/organizations/{orgId}/members`)
- **Request Body**:
```json
{
  "userId": "11112222-3333-4444-5555-666677778888",
  "role": "ADMIN"
}
```
- **Response 201 Created**:
```json
{
  "id": "33334444-5555-6666-7777-888899990000",
  "organizationId": "7f1c9d8a-5678-4321-98ba-fe0123456789",
  "userId": "11112222-3333-4444-5555-666677778888",
  "role": "ADMIN",
  "status": "ACTIVE",
  "createdAt": "2026-03-30T10:20:00Z"
}
```

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 4 README](README.md) — Tổng quan phân hệ Identity & Access
- [Phase 4.1: Users & BCrypt Hashing](phase4.1.md) — Quản lý tài khoản người dùng
- [Phase 4.3: Public Port Decoupling](phase4.3.md) — Giao tiếp giữa module `organization` và `identity`
- [Phase 4.6: Tenant Resolution Context](phase4.6.md) — Trích xuất ngữ cảnh tổ chức qua request header

