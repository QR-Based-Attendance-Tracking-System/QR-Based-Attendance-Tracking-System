CREATE TABLE student_courses (
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_student_courses PRIMARY KEY (student_id, course_id),
    CONSTRAINT fk_student_courses_student FOREIGN KEY (student_id)
        REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_courses_course FOREIGN KEY (course_id)
        REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX idx_student_courses_course ON student_courses(course_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_student_session'
          AND conrelid = 'attendance_records'::regclass
    ) THEN
        ALTER TABLE attendance_records
            ADD CONSTRAINT uq_student_session UNIQUE (session_id, student_id);
    END IF;
END $$;
