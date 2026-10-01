package lk.ruhunaefac.qrattendance.auth.dto;

import lk.ruhunaefac.qrattendance.user.entity.User;

public record CurrentUserResponse(String username, String email, String role, String fullName, String institutionalId, String department) {
    public static CurrentUserResponse from(User user) {
        return switch (user.getRole()) {
            case STUDENT -> new CurrentUserResponse(user.getUsername(), user.getEmail(), "STUDENT",
                    user.getStudent().getFullName(), user.getStudent().getStudentId(),
                    user.getStudent().getDepartment() == null ? null : user.getStudent().getDepartment().name());
            case LECTURER -> new CurrentUserResponse(user.getUsername(), user.getEmail(), "LECTURER",
                    user.getLecturer().getFullName(), user.getLecturer().getLecturerId(), null);
            case ADMIN -> new CurrentUserResponse(user.getUsername(), user.getEmail(), "ADMIN", null, null, null);
        };
    }
}
