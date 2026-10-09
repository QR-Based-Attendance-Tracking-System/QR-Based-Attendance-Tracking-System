ALTER TABLE attendance_sessions
    ADD COLUMN scheduled_date DATE,
    ADD COLUMN scheduled_start_time TIME,
    ADD COLUMN duration_minutes INTEGER;

-- Preserve the schedule represented by existing sessions before making the
-- new fields required by the entity.
UPDATE attendance_sessions
SET scheduled_date = (started_at AT TIME ZONE 'Asia/Colombo')::date,
    scheduled_start_time = (started_at AT TIME ZONE 'Asia/Colombo')::time,
    duration_minutes = CASE
        WHEN ended_at IS NOT NULL THEN GREATEST(1, CEIL(EXTRACT(EPOCH FROM (ended_at - started_at)) / 60)::INTEGER)
        ELSE 60
    END;

ALTER TABLE attendance_sessions
    ALTER COLUMN scheduled_date SET NOT NULL,
    ALTER COLUMN scheduled_start_time SET NOT NULL,
    ALTER COLUMN duration_minutes SET NOT NULL,
    ADD CONSTRAINT attendance_sessions_duration_positive CHECK (duration_minutes > 0);
