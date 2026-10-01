package lk.ruhunaefac.qrattendance.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import java.util.UUID;

public record StartAttendanceSessionRequest(
        @NotBlank String courseCode,
        UUID lectureHallId,
        String lectureHallName) {
    @AssertTrue(message = "A lecture hall ID or name is required")
    public boolean isLectureHallProvided() { return lectureHallId != null || (lectureHallName != null && !lectureHallName.isBlank()); }
}
