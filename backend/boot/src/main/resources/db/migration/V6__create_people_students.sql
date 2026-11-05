-- =============================================================================
-- Migration V6: Bảng học sinh trong schema people
-- =============================================================================

CREATE TABLE people.students (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    student_code VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    organization_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_students_code UNIQUE (student_code),
    CONSTRAINT chk_student_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_students_organization_id ON people.students (organization_id);
CREATE INDEX idx_students_user_id ON people.students (user_id);
CREATE INDEX idx_students_status ON people.students (status);

COMMENT ON TABLE people.students IS 'Hồ sơ học sinh gắn với tổ chức và tài khoản identity';

