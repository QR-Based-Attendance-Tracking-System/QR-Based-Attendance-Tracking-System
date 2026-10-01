ALTER TABLE users ADD COLUMN email VARCHAR(254);
CREATE UNIQUE INDEX uq_users_username_lower ON users (LOWER(username));
CREATE UNIQUE INDEX uq_students_student_id_lower ON students (LOWER(student_id));
CREATE UNIQUE INDEX uq_lecturers_lecturer_id_lower ON lecturers (LOWER(lecturer_id));
CREATE UNIQUE INDEX uq_users_email_lower ON users (LOWER(email)) WHERE email IS NOT NULL;

CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT auth_sessions_expiry_check CHECK (expires_at > created_at)
);
CREATE INDEX idx_auth_sessions_user ON auth_sessions(user_id);
CREATE INDEX idx_auth_sessions_expiry ON auth_sessions(expires_at);
