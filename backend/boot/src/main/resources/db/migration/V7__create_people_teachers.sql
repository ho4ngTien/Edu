-- =============================================================================
-- Migration V7: Bảng giáo viên trong schema people
-- =============================================================================

CREATE TABLE people.teachers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    teacher_code VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255),
    organization_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_teachers_code UNIQUE (teacher_code),
    CONSTRAINT chk_teacher_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE'))
);

CREATE INDEX idx_teachers_organization_id ON people.teachers (organization_id);
CREATE INDEX idx_teachers_user_id ON people.teachers (user_id);

COMMENT ON TABLE people.teachers IS 'Hồ sơ giáo viên gắn với tổ chức và tài khoản identity';

