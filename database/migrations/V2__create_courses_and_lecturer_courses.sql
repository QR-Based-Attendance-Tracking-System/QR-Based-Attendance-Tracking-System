CREATE TABLE courses (
    id UUID PRIMARY KEY,
    course_code VARCHAR(50) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE lecturer_courses (
    lecturer_id UUID NOT NULL,
    course_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_lecturer_courses PRIMARY KEY (lecturer_id, course_id),
    CONSTRAINT fk_lecturer_courses_lecturer FOREIGN KEY (lecturer_id)
        REFERENCES lecturers(id) ON DELETE CASCADE,
    CONSTRAINT fk_lecturer_courses_course FOREIGN KEY (course_id)
        REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX idx_lecturer_courses_course ON lecturer_courses(course_id);
