-- =============================================================================
-- Migration V9: Bảng học kỳ và lớp học trong schema academic
-- =============================================================================

CREATE TABLE academic.semesters (
    id UUID PRIMARY KEY,
    academic_year_id UUID NOT NULL REFERENCES academic.academic_years(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    organization_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_semester_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_semesters_academic_year ON academic.semesters (academic_year_id);
CREATE INDEX idx_semesters_org ON academic.semesters (organization_id);

CREATE TABLE academic.class_rooms (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    organization_id UUID NOT NULL,
    academic_year_id UUID NOT NULL REFERENCES academic.academic_years(id),
    capacity INTEGER CHECK (capacity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_class_code_per_org UNIQUE (code, organization_id)
);

CREATE INDEX idx_class_rooms_org ON academic.class_rooms (organization_id);
CREATE INDEX idx_class_rooms_academic_year ON academic.class_rooms (academic_year_id);

COMMENT ON TABLE academic.semesters IS 'Học kỳ trực thuộc năm học';
COMMENT ON TABLE academic.class_rooms IS 'Lớp học trực thuộc năm học và tổ chức';

