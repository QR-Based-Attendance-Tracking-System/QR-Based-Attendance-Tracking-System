package lk.ruhunaefac.qrattendance.attendance.session.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;

public record AttendanceSessionResponse(UUID id, String courseCode, String lecturerName,
                                       UUID lectureHallId, Instant startedAt, LocalDate date, LocalTime startTime,
                                       int durationMinutes, Instant endedAt,
                                       boolean active) {
    public static AttendanceSessionResponse from(AttendanceSession session) {
        return new AttendanceSessionResponse(session.getId(), session.getCourseCode(), session.getLecturerName(),
                session.getLectureHall().getId(), session.getStartedAt(), session.getScheduledDate(),
                session.getScheduledStartTime(), session.getDurationMinutes(), session.getEndedAt(), session.isActive());
    }
}
