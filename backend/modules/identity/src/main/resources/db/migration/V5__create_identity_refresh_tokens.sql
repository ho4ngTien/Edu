-- Migration V5: Create table identity.refresh_tokens
-- Schema: identity
-- Purpose: Quản lý vòng đời Opaque Refresh Token, Rotation và Token Family

CREATE TABLE identity.refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ NULL,
    revoked_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_refresh_tokens_expires_after_created CHECK (expires_at > created_at)
);

-- Chỉ mục tối ưu hóa tra cứu theo user, family và token hash
CREATE INDEX idx_refresh_tokens_user_id ON identity.refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON identity.refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_token_hash ON identity.refresh_tokens (token_hash);
