-- =============================================================================
-- Migration V8: Bảng năm học trong schema academic
-- =============================================================================

CREATE TABLE academic.academic_years (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    organization_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_academic_year_dates CHECK (end_date > start_date),
    CONSTRAINT chk_academic_year_status CHECK (status IN ('UPCOMING', 'ACTIVE', 'CLOSED', 'ARCHIVED'))
);

CREATE INDEX idx_academic_years_org ON academic.academic_years (organization_id);
CREATE INDEX idx_academic_years_status ON academic.academic_years (status);

COMMENT ON TABLE academic.academic_years IS 'Quản lý năm học theo tổ chức';

