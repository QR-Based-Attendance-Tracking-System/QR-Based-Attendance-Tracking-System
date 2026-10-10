package lk.ruhunaefac.qrattendance.attendance.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record CodeCheckInRequest(@NotNull UUID sessionId, @NotBlank @Pattern(regexp = "\\d{6}") String code) { }
