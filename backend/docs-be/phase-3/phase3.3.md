# Phase 3.3 — Flyway Migrations Đa Schema (`identity`, `organization`)

> **Dự án:** E-Edu Backend · Java 21 + Spring Boot 3.4 · Modular Monolith  
> **Cơ sở dữ liệu:** PostgreSQL 16  
> **Trạng thái:** ✅ Hoàn thành (V1–V5), 🗓 V6 Đang lên kế hoạch

---

## 1. Chiến lược Quản lý Database Schema

### Tại sao dùng Flyway?

| Vấn đề | Giải pháp với Flyway |
|---|---|
| Schema thay đổi không được version-controlled | Mỗi thay đổi là một migration file có version |
| `ddl-auto: create-drop` phá dữ liệu production | `ddl-auto: validate` + Flyway chạy migrations |
| Khó rollback thay đổi schema | Migration files trong Git, có thể review và revert |
| Đa môi trường (dev/staging/prod) có schema khác nhau | Flyway đảm bảo tất cả môi trường ở cùng version |
| Đa schema trong một DB instance | Flyway migrate từng schema với locations riêng |

### Nguyên tắc cốt lõi

1. **Chỉ tiến về phía trước** — Không sửa migration đã chạy (checksum bảo vệ)
2. **Mỗi migration là atomic** — Nếu thất bại, rollback toàn bộ migration đó
3. **Migration files trong SCM** — Luôn commit cùng với code thay đổi model
4. **Kiểm tra schema tại startup** — Hibernate `validate` xác nhận schema khớp entity

---

## 2. Migration Naming Convention

```
V{n}__{description}.sql
 │       │
 │       └── Mô tả ngắn gọn, snake_case, không dấu tiếng Việt
 └── Version number, tăng dần, không được trùng
```

### Quy tắc đặt tên:

| Quy tắc | Ví dụ đúng | Ví dụ sai |
|---|---|---|
| Prefix `V` (viết hoa) | `V1__create_initial_schemas.sql` | `v1__init.sql` |
| Hai dấu gạch dưới | `V2__create_users_table.sql` | `V2_create_users.sql` |
| Snake_case cho description | `V4__create_memberships_table.sql` | `V4__CreateMemberships.sql` |
| Mô tả rõ ràng hành động | `V5__add_refresh_tokens.sql` | `V5__update.sql` |
| Không dùng ký tự đặc biệt | `V3__create_organizations.sql` | `V3__create-orgs!.sql` |

---

## 3. Cấu trúc Thư mục

```
src/
└── main/
    └── resources/
        └── db/
            └── migration/
                ├── V1__create_initial_schemas.sql
                ├── V2__create_users_table.sql
                ├── V3__create_organizations_table.sql
                ├── V4__create_organization_memberships_table.sql
                ├── V5__create_refresh_tokens_table.sql
                └── (V6__create_rbac_tables.sql)  ← Kế hoạch
```

---

## 4. Danh mục Migrations Hiện tại

### V1 — Tạo Initial Schemas

**File:** `V1__create_initial_schemas.sql`

```sql
-- ============================================================
-- V1: Khởi tạo các schema riêng biệt cho từng bounded context
-- ============================================================

-- Schema cho Identity bounded context (users, auth)
CREATE SCHEMA IF NOT EXISTS identity;

-- Schema cho Organization bounded context (orgs, memberships)
CREATE SCHEMA IF NOT EXISTS organization;

-- Comment mô tả mục đích
COMMENT ON SCHEMA identity IS 'Bounded context quản lý danh tính người dùng và xác thực';
COMMENT ON SCHEMA organization IS 'Bounded context quản lý tổ chức và thành viên';
```

---

### V2 — Bảng `identity.users`

**File:** `V2__create_users_table.sql`

```sql
-- ============================================================
-- V2: Tạo bảng users trong schema identity
-- ============================================================

CREATE TABLE identity.users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL,
    email        VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT chk_users_username_length CHECK (LENGTH(username) >= 3)
);

-- Index cho lookup thường dùng
CREATE INDEX idx_users_email    ON identity.users (email);
CREATE INDEX idx_users_username ON identity.users (username);
CREATE INDEX idx_users_active   ON identity.users (is_active) WHERE is_active = TRUE;

COMMENT ON TABLE  identity.users              IS 'Tài khoản người dùng trong hệ thống E-Edu';
COMMENT ON COLUMN identity.users.password_hash IS 'Mật khẩu đã được hash bằng BCrypt';
COMMENT ON COLUMN identity.users.is_active     IS 'FALSE = tài khoản bị vô hiệu hóa (soft delete)';
```

---

### V3 — Bảng `organization.organizations`

**File:** `V3__create_organizations_table.sql`

```sql
-- ============================================================
-- V3: Tạo bảng organizations trong schema organization
-- ============================================================

CREATE TYPE organization.org_type AS ENUM (
    'SCHOOL',       -- Trường học
    'COMPANY',      -- Công ty / Doanh nghiệp
    'DEPARTMENT',   -- Phòng ban
    'PERSONAL'      -- Tổ chức cá nhân / nhóm nhỏ
);

CREATE TABLE organization.organizations (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(200)           NOT NULL,
    code         VARCHAR(50)            NOT NULL,
    description  TEXT,
    type         organization.org_type  NOT NULL DEFAULT 'SCHOOL',
    owner_id     BIGINT                 NOT NULL,  -- FK → identity.users.id (cross-schema reference)
    is_active    BOOLEAN                NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ            NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_organizations_code UNIQUE (code),
    CONSTRAINT chk_org_code_format CHECK (code ~ '^[A-Z0-9_]+$')
);

-- Indexes
CREATE INDEX idx_organizations_code     ON organization.organizations (code);
CREATE INDEX idx_organizations_owner    ON organization.organizations (owner_id);
CREATE INDEX idx_organizations_type     ON organization.organizations (type);
CREATE INDEX idx_organizations_active   ON organization.organizations (is_active)
    WHERE is_active = TRUE;

COMMENT ON TABLE  organization.organizations         IS 'Tổ chức (trường học, công ty, phòng ban) trong E-Edu';
COMMENT ON COLUMN organization.organizations.owner_id IS 'ID của người tạo/sở hữu tổ chức (tham chiếu identity.users)';
COMMENT ON COLUMN organization.organizations.code     IS 'Mã định danh duy nhất, chỉ in hoa và số';
```

---

### V4 — Bảng `organization.organization_memberships`

**File:** `V4__create_organization_memberships_table.sql`

```sql
-- ============================================================
-- V4: Tạo bảng tư cách thành viên tổ chức
-- ============================================================

CREATE TYPE organization.membership_status AS ENUM (
    'PENDING',   -- Đang chờ phê duyệt
    'ACTIVE',    -- Đang hoạt động
    'SUSPENDED', -- Bị tạm đình chỉ
    'LEFT'       -- Đã rời tổ chức
);

CREATE TABLE organization.organization_memberships (
    id              BIGSERIAL PRIMARY KEY,
    organization_id BIGINT                          NOT NULL
        REFERENCES organization.organizations (id) ON DELETE CASCADE,
    user_id         BIGINT                          NOT NULL,  -- FK cross-schema → identity.users
    status          organization.membership_status  NOT NULL DEFAULT 'PENDING',
    joined_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ                     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ                     NOT NULL DEFAULT NOW(),

    -- Mỗi user chỉ có một membership trong một tổ chức
    CONSTRAINT uq_membership_org_user UNIQUE (organization_id, user_id)
);

-- Indexes
CREATE INDEX idx_memberships_org    ON organization.organization_memberships (organization_id);
CREATE INDEX idx_memberships_user   ON organization.organization_memberships (user_id);
CREATE INDEX idx_memberships_status ON organization.organization_memberships (status);

COMMENT ON TABLE organization.organization_memberships IS
    'Quan hệ thành viên giữa người dùng và tổ chức';
COMMENT ON COLUMN organization.organization_memberships.user_id IS
    'ID người dùng từ schema identity (cross-schema reference, không có FK vật lý)';
```

---

### V5 — Bảng `identity.refresh_tokens`

**File:** `V5__create_refresh_tokens_table.sql`

```sql
-- ============================================================
-- V5: Tạo bảng refresh tokens cho JWT authentication
-- ============================================================

CREATE TABLE identity.refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL
        REFERENCES identity.users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL,
    device_info VARCHAR(500),
    ip_address  INET,
    issued_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked_at  TIMESTAMPTZ,               -- NULL = chưa bị thu hồi
    is_revoked  BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);

-- Indexes
CREATE INDEX idx_refresh_tokens_user     ON identity.refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_hash     ON identity.refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_expires  ON identity.refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_active   ON identity.refresh_tokens (user_id, is_revoked)
    WHERE is_revoked = FALSE;

COMMENT ON TABLE  identity.refresh_tokens             IS 'Refresh tokens cho JWT authentication flow';
COMMENT ON COLUMN identity.refresh_tokens.token_hash  IS 'Hash SHA-256 của refresh token (không lưu plain text)';
COMMENT ON COLUMN identity.refresh_tokens.device_info IS 'User-Agent hoặc thông tin thiết bị phát hành token';
COMMENT ON COLUMN identity.refresh_tokens.revoked_at  IS 'Thời điểm token bị thu hồi (logout, security event)';
```

---

### V6 — RBAC Tables (Kế hoạch)

**File dự kiến:** `V6__create_rbac_tables.sql`

```sql
-- ============================================================
-- V6 (KẾ HOẠCH): Role-Based Access Control tables
-- ============================================================

-- Roles trong phạm vi tổ chức
CREATE TABLE organization.roles (
    id              BIGSERIAL    PRIMARY KEY,
    organization_id BIGINT       NOT NULL
        REFERENCES organization.organizations (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    is_system_role  BOOLEAN      NOT NULL DEFAULT FALSE,  -- Role mặc định hệ thống
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_role_org_name UNIQUE (organization_id, name)
);

-- Permissions (quyền hạn cụ thể)
CREATE TABLE organization.permissions (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,  -- e.g. 'course:create', 'member:invite'
    description TEXT,
    resource    VARCHAR(50)  NOT NULL,         -- e.g. 'course', 'member'
    action      VARCHAR(50)  NOT NULL          -- e.g. 'create', 'read', 'update', 'delete'
);

-- Gán permissions cho roles (many-to-many)
CREATE TABLE organization.role_permissions (
    role_id       BIGINT NOT NULL REFERENCES organization.roles (id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES organization.permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Gán roles cho memberships (many-to-many)
CREATE TABLE organization.membership_roles (
    membership_id BIGINT NOT NULL
        REFERENCES organization.organization_memberships (id) ON DELETE CASCADE,
    role_id       BIGINT NOT NULL
        REFERENCES organization.roles (id) ON DELETE CASCADE,
    granted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (membership_id, role_id)
);
```

---

## 5. Cấu hình Flyway trong `application.yml`

```yaml
spring:
  # ─── Datasource ──────────────────────────────────────────────────────────
  datasource:
    url: jdbc:postgresql://localhost:5432/edu_db
    username: ${DB_USERNAME:edu_user}
    password: ${DB_PASSWORD:edu_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  # ─── JPA / Hibernate ─────────────────────────────────────────────────────
  jpa:
    hibernate:
      # validate: Hibernate kiểm tra schema khớp với entity, KHÔNG tự tạo/sửa
      # Flyway chịu trách nhiệm tạo/sửa schema
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false  # Đặt true khi debug local

  # ─── Flyway ──────────────────────────────────────────────────────────────
  flyway:
    enabled: true
    # Thư mục chứa migration scripts
    locations: classpath:db/migration
    # Cho phép Flyway chạy trên DB có dữ liệu cũ (không có flyway_schema_history)
    baseline-on-migrate: true
    baseline-version: 0
    # Validate checksum của migration files đã chạy
    validate-on-migrate: true
    # Encoding của file SQL
    sql-migration-encoding: UTF-8
    # Prefix và suffix mặc định
    sql-migration-prefix: V
    sql-migration-separator: __
    sql-migration-suffixes: .sql
```

---

## 6. Kết hợp Flyway + Hibernate `validate`

```
Application Startup
        │
        ▼
┌──────────────────────────────┐
│  Flyway checks               │
│  flyway_schema_history table │
│                              │
│  Nếu có migration mới:       │
│  → Chạy V{n}__.sql file      │
│  → Ghi vào history table     │
└──────────┬───────────────────┘
           │ (sau khi tất cả migrations đã chạy)
           ▼
┌──────────────────────────────┐
│  Hibernate SessionFactory    │
│  khởi tạo với ddl-auto:      │
│  validate                    │
│                              │
│  So sánh Entity metadata     │
│  với actual schema trong DB  │
│                              │
│  Nếu không khớp → FAIL FAST  │
│  (không start application)   │
└──────────────────────────────┘
```

### Lợi ích của kết hợp này:

| Flyway lo | Hibernate lo |
|---|---|
| Tạo/alter table | Xác nhận schema đúng |
| Version control migration | Map entity ↔ table |
| Rollback-safe script | Relationship management |
| Cross-team collaboration | Type conversion |

---

## 7. Cross-Schema References

Trong PostgreSQL, một bảng ở schema A **có thể tham chiếu** bảng ở schema B thông qua tên đầy đủ (`schema.table`). Tuy nhiên, trong thiết kế này, tham chiếu cross-schema giữa `organization.organizations.owner_id` và `identity.users.id` được thiết kế là **logical reference** (không có FK vật lý) vì:

1. **Bounded context independence:** Organization không nên phụ thuộc vào internal structure của Identity
2. **Microservice-ready:** Nếu sau này tách service, không cần xóa FK
3. **Performance:** Tránh cross-schema join phức tạp

```
identity.users.id  ←── (logical reference, no FK constraint) ───  organization.organizations.owner_id
identity.users.id  ←── (logical reference, no FK constraint) ───  organization.organization_memberships.user_id
```

Integrity được đảm bảo qua Application layer (kiểm tra user tồn tại trước khi tạo organization/membership).

---

## 8. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
|:---:|---|---|
| **AC-3.3.1** | Application khởi động thành công với Flyway | Log thấy `Flyway Community Edition ... has successfully applied N migrations` |
| **AC-3.3.2** | Schema `identity` và `organization` được tạo | `SELECT schema_name FROM information_schema.schemata` trả về cả hai |
| **AC-3.3.3** | Tất cả V1–V5 tables được tạo đúng cấu trúc | `\dt identity.*` và `\dt organization.*` trong psql liệt kê đúng tables |
| **AC-3.3.4** | Unique constraints hoạt động | INSERT duplicate username/email/org_code → `duplicate key value violates unique constraint` |
| **AC-3.3.5** | Flyway history được ghi nhận | `SELECT * FROM flyway_schema_history` có 5 records với `success = TRUE` |
| **AC-3.3.6** | Hibernate validate không báo lỗi | Log không có `Schema-validation: missing table`, `missing column` |
| **AC-3.3.7** | Migration files có checksum ổn định | Sửa file đã migrate → application không start, báo `checksum mismatch` |
| **AC-3.3.8** | `baseline-on-migrate: true` hoạt động | DB có dữ liệu cũ (không có flyway_schema_history) vẫn migrate thành công |

---

## 9. Liên kết Tài liệu Liên quan

| Tài liệu | Mô tả |
|---|---|
| [Phase 3 README](README.md) | Index tổng thể Phase 3 |
| [Phase 3.1 — Global Exception Handling](phase3.1.md) | `ErrorCode.ORG_NOT_FOUND`, `USER_NOT_FOUND` liên quan đến entity trong migration này |

