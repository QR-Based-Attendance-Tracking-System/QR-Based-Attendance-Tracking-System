ALTER TABLE attendance_sessions
    ADD COLUMN qr_window_started_at TIMESTAMPTZ;

-- Existing sessions retain their previous start as the best available anchor.
UPDATE attendance_sessions
SET qr_window_started_at = started_at;
