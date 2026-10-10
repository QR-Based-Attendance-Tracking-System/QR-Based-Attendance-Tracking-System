package lk.ruhunaefac.qrattendance.attendance.checkin.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(@NotBlank String qrToken) {
}
