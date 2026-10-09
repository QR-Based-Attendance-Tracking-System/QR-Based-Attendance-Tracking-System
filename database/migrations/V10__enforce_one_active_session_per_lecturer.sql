CREATE UNIQUE INDEX uq_attendance_sessions_one_active_per_lecturer
    ON attendance_sessions (lecturer_name)
    WHERE active = TRUE;
