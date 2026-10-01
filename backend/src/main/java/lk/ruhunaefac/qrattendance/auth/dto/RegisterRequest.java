package lk.ruhunaefac.qrattendance.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotBlank @Size(min = 12, max = 128) String confirmPassword,
        @NotBlank @Size(max = 100) String institutionalId,
        @NotBlank @Size(max = 200) String registrationCode,
        lk.ruhunaefac.qrattendance.user.entity.Student.Department department) {
    @Override public String toString() { return "RegisterRequest[sensitive fields redacted]"; }
}
