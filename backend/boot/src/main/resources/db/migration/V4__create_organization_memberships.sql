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
