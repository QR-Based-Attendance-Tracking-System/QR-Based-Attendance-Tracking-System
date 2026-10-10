package lk.ruhunaefac.qrattendance.attendance.reporting.dto;

import java.time.Instant;
import java.util.List;

public record LecturerDashboardResponse(
        String lecturerName,
        long totalCourses,
        long totalSessions,
        long totalCheckIns,
        long activeSessions,
        List<CourseSummary> courses,
        List<SessionSummary> recentSessions) {

    public record CourseSummary(String courseCode, long sessionCount, long checkInCount, Instant latestSessionAt) { }
    public record SessionSummary(String id, String courseCode, Instant startedAt, boolean active, long checkInCount) { }
}
