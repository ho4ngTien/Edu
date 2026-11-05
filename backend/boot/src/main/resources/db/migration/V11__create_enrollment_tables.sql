-- =============================================================================
-- Migration V11: Bảng đăng ký học phần và nhóm học tập trong schema enrollment
-- =============================================================================

CREATE TABLE enrollment.enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    class_room_id UUID NOT NULL,
    semester_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    withdrawn_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_enrollment_student_subject_semester UNIQUE (student_id, subject_id, semester_id),
    CONSTRAINT chk_enrollment_status CHECK (status IN ('ENROLLED', 'WITHDRAWN', 'COMPLETED'))
);

CREATE INDEX idx_enrollments_student ON enrollment.enrollments (student_id);
CREATE INDEX idx_enrollments_subject ON enrollment.enrollments (subject_id);
CREATE INDEX idx_enrollments_class_room ON enrollment.enrollments (class_room_id);
CREATE INDEX idx_enrollments_semester ON enrollment.enrollments (semester_id);
CREATE INDEX idx_enrollments_org_status ON enrollment.enrollments (organization_id, status);

CREATE TABLE enrollment.learning_groups (
    id UUID PRIMARY KEY,
    class_room_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    organization_id UUID NOT NULL,
    max_members INTEGER CHECK (max_members > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_learning_groups_class_room ON enrollment.learning_groups (class_room_id);
CREATE INDEX idx_learning_groups_org ON enrollment.learning_groups (organization_id);

COMMENT ON TABLE enrollment.enrollments IS 'Đăng ký học phần của học sinh';
COMMENT ON TABLE enrollment.learning_groups IS 'Nhóm học tập trong lớp';

