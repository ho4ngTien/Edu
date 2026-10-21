-- =============================================================================
-- Migration V2: Bảng người dùng trong schema identity
-- =============================================================================

CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);

CREATE INDEX idx_users_username ON identity.users (username);
CREATE INDEX idx_users_email ON identity.users (email);

COMMENT ON TABLE identity.users IS 'Tài khoản người dùng toàn hệ thống';
COMMENT ON COLUMN identity.users.password_hash IS 'Mật khẩu được mã hóa BCrypt, không lưu plaintext';
