-- =============================================================================
-- Migration V10: Bảng môn học và chương trình đào tạo trong schema curriculum
-- =============================================================================

CREATE TABLE curriculum.subjects (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    credits INTEGER NOT NULL CHECK (credits > 0),
    organization_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_subject_code_per_org UNIQUE (code, organization_id),
    CONSTRAINT chk_subject_status CHECK (status IN ('ACTIVE', 'DEPRECATED'))
);

CREATE INDEX idx_subjects_org ON curriculum.subjects (organization_id);
CREATE INDEX idx_subjects_status ON curriculum.subjects (status);

CREATE TABLE curriculum.curricula (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    total_credits INTEGER NOT NULL CHECK (total_credits > 0),
    organization_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_curriculum_code_per_org UNIQUE (code, organization_id)
);

CREATE INDEX idx_curricula_org ON curriculum.curricula (organization_id);

CREATE TABLE curriculum.curriculum_subjects (
    curriculum_id UUID NOT NULL REFERENCES curriculum.curricula(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES curriculum.subjects(id) ON DELETE RESTRICT,
    is_required BOOLEAN NOT NULL DEFAULT true,
    sequence_order INTEGER,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (curriculum_id, subject_id)
);

CREATE INDEX idx_curriculum_subjects_curriculum ON curriculum.curriculum_subjects (curriculum_id);
CREATE INDEX idx_curriculum_subjects_subject ON curriculum.curriculum_subjects (subject_id);

COMMENT ON TABLE curriculum.subjects IS 'Danh mục môn học';
COMMENT ON TABLE curriculum.curricula IS 'Chương trình đào tạo';
COMMENT ON TABLE curriculum.curriculum_subjects IS 'Môn học thuộc chương trình đào tạo';

