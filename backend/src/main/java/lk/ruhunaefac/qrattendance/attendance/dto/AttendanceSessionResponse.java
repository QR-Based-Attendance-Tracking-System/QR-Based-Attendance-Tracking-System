package lk.ruhunaefac.qrattendance.attendance.dto;

import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceSession;

public record AttendanceSessionResponse(UUID id, String courseCode, String lecturerName,
                                       UUID lectureHallId, Instant startedAt, Instant endedAt,
                                       boolean active) {
    public static AttendanceSessionResponse from(AttendanceSession session) {
        return new AttendanceSessionResponse(session.getId(), session.getCourseCode(), session.getLecturerName(),
                session.getLectureHall().getId(), session.getStartedAt(), session.getEndedAt(), session.isActive());
    }
}
