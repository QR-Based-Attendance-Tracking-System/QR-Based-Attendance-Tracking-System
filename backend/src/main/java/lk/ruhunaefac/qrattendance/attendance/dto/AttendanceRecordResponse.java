package lk.ruhunaefac.qrattendance.attendance.dto;

import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceRecord;

public record AttendanceRecordResponse(UUID id, UUID sessionId, String studentId,
                                       Instant checkedInAt, String status) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(record.getId(), record.getSession().getId(), record.getStudentId(),
                record.getCheckedInAt(), record.getStatus());
    }
}
