package lk.ruhunaefac.qrattendance.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartAttendanceSessionRequest(
        @NotBlank String courseCode,
        @NotBlank String lecturerName,
        @NotNull UUID lectureHallId) {
}
