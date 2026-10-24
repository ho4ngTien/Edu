# Phase 2.5: Database Schemas & Flyway Migrations

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.1. Database Schemas & Flyway Migrations

Cấu trúc cơ sở dữ liệu kế thừa migration `V1__create_initial_schemas.sql` (Phase 2.3). Các migration tiếp theo tuân theo version tuần tự tập trung tại `boot/src/main/resources/db/migration/` (hoặc cấu hình classpath đa module tương ứng).

#### Migration 1: `V2__create_identity_users.sql`

```sql
-- =============================================================================
-- Migration V2: Bảng người dùng trong schema identity
-- =============================================================================

CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);

CREATE INDEX idx_users_username ON identity.users (username);
CREATE INDEX idx_users_email ON identity.users (email);

COMMENT ON TABLE identity.users IS 'Tài khoản người dùng toàn hệ thống';
COMMENT ON COLUMN identity.users.password_hash IS 'Mật khẩu được mã hóa BCrypt, không lưu plaintext';
```

#### Migration 2: `V3__create_organizations.sql`

```sql
-- =============================================================================
-- Migration V3: Bảng tổ chức trong schema organization
-- =============================================================================

CREATE TABLE organization.organizations (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_organizations_code UNIQUE (code),
    CONSTRAINT ck_organizations_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

CREATE INDEX idx_organizations_code ON organization.organizations (code);

COMMENT ON TABLE organization.organizations IS 'Đơn vị trường học, viện đào tạo hoặc phân hiệu';
COMMENT ON COLUMN organization.organizations.code IS 'Mã định danh duy nhất của tổ chức trong hệ thống';
```

#### Migration 3: `V4__create_organization_memberships.sql`

```sql
-- =============================================================================
-- Migration V4: Bảng tư cách thành viên trong schema organization
-- =============================================================================

CREATE TABLE organization.organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'INVITED',
    role VARCHAR(30) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_memberships_organization_user UNIQUE (organization_id, user_id),
    CONSTRAINT ck_memberships_status CHECK (status IN ('INVITED', 'ACTIVE', 'SUSPENDED', 'LEFT')),
    CONSTRAINT ck_memberships_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
    CONSTRAINT fk_memberships_organization FOREIGN KEY (organization_id)
        REFERENCES organization.organizations (id) ON DELETE CASCADE
);

CREATE INDEX idx_memberships_user_status ON organization.organization_memberships (user_id, status);
CREATE INDEX idx_memberships_org_status ON organization.organization_memberships (organization_id, status);

COMMENT ON TABLE organization.organization_memberships IS 'Liên kết giữa người dùng và tổ chức kèm vai trò';
COMMENT ON COLUMN organization.organization_memberships.user_id IS 'Logical reference tới identity.users.id (Không tạo Cross-Schema FK vật lý)';
```

> [!NOTE]
> Bảng `organization_memberships` có ràng buộc Foreign Key nội bộ tới `organization.organizations(id)`, nhưng **tuyệt đối không** tạo Physical Foreign Key sang `identity.users(id)`. Toàn vẹn tham chiếu được kiểm soát ở application layer qua `OrganizationAccountQuery`.

---
