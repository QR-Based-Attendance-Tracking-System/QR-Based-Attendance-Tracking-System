CREATE TABLE users (
    id UUID PRIMARY KEY,

    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT users_role_check
        CHECK (role IN ('STUDENT', 'LECTURER', 'ADMIN'))
);

CREATE TABLE students (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL UNIQUE,

    student_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE lecturers (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL UNIQUE,

    lecturer_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,

    CONSTRAINT fk_lecturer_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE admins (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL UNIQUE,

    admin_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,

    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE lecture_halls (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    geofence_radius_meters DOUBLE PRECISION,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE attendance_sessions (
    id UUID PRIMARY KEY,

    course_code VARCHAR(50) NOT NULL,
    lecturer_name VARCHAR(150) NOT NULL,

    lecture_hall_id UUID NOT NULL,

    secret_key BYTEA NOT NULL,

    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_attendance_session_hall
        FOREIGN KEY (lecture_hall_id)
        REFERENCES lecture_halls(id),

    CONSTRAINT attendance_sessions_active_check
        CHECK (
            (active = TRUE AND ended_at IS NULL)
            OR
            (active = FALSE AND ended_at IS NOT NULL)
        )
);

CREATE INDEX idx_attendance_sessions_active
    ON attendance_sessions(active);

CREATE INDEX idx_attendance_sessions_hall
    ON attendance_sessions(lecture_hall_id);

CREATE TABLE attendance_records (
    id UUID PRIMARY KEY,

    session_id UUID NOT NULL,

    student_id VARCHAR(100) NOT NULL,

    checked_in_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',

    CONSTRAINT fk_attendance_session
        FOREIGN KEY (session_id)
        REFERENCES attendance_sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_student_session
        UNIQUE (session_id, student_id)
);

CREATE INDEX idx_attendance_records_session
    ON attendance_records(session_id);

CREATE INDEX idx_attendance_records_student
    ON attendance_records(student_id);