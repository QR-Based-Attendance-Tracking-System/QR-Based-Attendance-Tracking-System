package lk.ruhunaefac.qrattendance.attendance.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record StartAttendanceSessionRequest(
        @NotBlank String courseCode,
        @NotBlank String lectureHallName,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @NotNull @Min(1) Integer durationMinutes) {
}
