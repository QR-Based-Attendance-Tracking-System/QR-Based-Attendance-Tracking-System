package lk.ruhunaefac.qrattendance.attendance.reporting.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.checkin.entity.AttendanceRecord;
import lk.ruhunaefac.qrattendance.attendance.checkin.repository.AttendanceRecordRepository;
import lk.ruhunaefac.qrattendance.attendance.reporting.dto.LecturerDashboardResponse;
import lk.ruhunaefac.qrattendance.attendance.service.AttendanceSessionLifecycleService;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.attendance.session.repository.AttendanceSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LecturerAttendanceReportingService {
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionLifecycleService lifecycle;

    public LecturerAttendanceReportingService(AttendanceSessionRepository sessionRepository,
                                              AttendanceRecordRepository recordRepository,
                                              AttendanceSessionLifecycleService lifecycle) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.lifecycle = lifecycle;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getRecords(UUID sessionId, String lecturerUsername) {
        lifecycle.getOwnedSession(sessionId, lecturerUsername);
        return recordRepository.findBySessionIdOrderByCheckedInAtDesc(sessionId);
    }

    @Transactional
    public LecturerDashboardResponse getLecturerDashboard(String lecturerName) {
        List<AttendanceSession> sessions = sessionRepository.findByLecturerNameOrderByStartedAtDesc(lecturerName);
        Instant now = Instant.now();
        sessions.forEach(session -> {
            lifecycle.activateIfScheduled(session, now);
            lifecycle.endIfExpired(session, now);
        });
        List<UUID> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
        Map<UUID, Long> checkInsBySession = new HashMap<>();
        if (!sessionIds.isEmpty()) {
            recordRepository.countBySessionIds(sessionIds).forEach(count ->
                    checkInsBySession.put(count.getSessionId(), count.getAttendanceCount()));
        }

        Map<String, CourseAccumulator> courses = new LinkedHashMap<>();
        long totalCheckIns = 0;
        long activeSessions = 0;
        for (AttendanceSession session : sessions) {
            long checkIns = checkInsBySession.getOrDefault(session.getId(), 0L);
            totalCheckIns += checkIns;
            if (session.isActive()) activeSessions++;
            courses.computeIfAbsent(session.getCourseCode(), ignored -> new CourseAccumulator(session.getStartedAt()))
                    .add(checkIns);
        }
        List<LecturerDashboardResponse.CourseSummary> courseSummaries = courses.entrySet().stream()
                .map(entry -> new LecturerDashboardResponse.CourseSummary(entry.getKey(), entry.getValue().sessions,
                        entry.getValue().checkIns, entry.getValue().latestSessionAt)).toList();
        List<LecturerDashboardResponse.SessionSummary> recentSessions = sessions.stream().limit(5)
                .map(session -> new LecturerDashboardResponse.SessionSummary(session.getId().toString(),
                        session.getCourseCode(), session.getStartedAt(), session.isActive(),
                        checkInsBySession.getOrDefault(session.getId(), 0L))).toList();
        String displayName = sessions.isEmpty() ? lecturerName : sessions.getFirst().getLecturerName();
        return new LecturerDashboardResponse(displayName, courses.size(), sessions.size(), totalCheckIns,
                activeSessions, courseSummaries, recentSessions);
    }

    private static final class CourseAccumulator {
        private final Instant latestSessionAt;
        private long sessions;
        private long checkIns;
        private CourseAccumulator(Instant latestSessionAt) { this.latestSessionAt = latestSessionAt; }
        private void add(long count) { sessions++; checkIns += count; }
    }
}
