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
