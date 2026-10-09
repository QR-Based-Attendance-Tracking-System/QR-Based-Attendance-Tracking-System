package lk.ruhunaefac.qrattendance.attendance.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceRecord;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.attendance.dto.QrChallengeResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.LecturerDashboardResponse;
import lk.ruhunaefac.qrattendance.attendance.exception.AttendanceAlreadyMarkedException;
import lk.ruhunaefac.qrattendance.attendance.exception.StudentNotEnrolledException;
import lk.ruhunaefac.qrattendance.attendance.repository.AttendanceRecordRepository;
import lk.ruhunaefac.qrattendance.attendance.repository.AttendanceSessionRepository;
import lk.ruhunaefac.qrattendance.lecturehall.entity.LectureHall;
import lk.ruhunaefac.qrattendance.lecturehall.repository.LectureHallRepository;
import lk.ruhunaefac.qrattendance.user.entity.Student;
import lk.ruhunaefac.qrattendance.user.repository.StudentRepository;
import lk.ruhunaefac.qrattendance.user.repository.LecturerRepository;
import lk.ruhunaefac.qrattendance.qr.service.QrTokenService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final LectureHallRepository lectureHallRepository;
    private final QrTokenService qrTokenService;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;

    public AttendanceService(AttendanceSessionRepository sessionRepository, AttendanceRecordRepository recordRepository,
                             LectureHallRepository lectureHallRepository, QrTokenService qrTokenService,
                             StudentRepository studentRepository, LecturerRepository lecturerRepository) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.lectureHallRepository = lectureHallRepository;
        this.qrTokenService = qrTokenService;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
    }

    @Transactional
    public AttendanceSession startSession(String courseCode, String lecturerName, String lectureHallName,
                                          LocalDate date, LocalTime startTime,
                                          int durationMinutes) {
        if (durationMinutes <= 0) throw new IllegalArgumentException("Duration must be greater than zero");
        if (date == null || startTime == null) throw new IllegalArgumentException("Session date and start time are required");
        boolean assignedCourse = lecturerRepository.findByUserUsername(lecturerName)
                .map(lecturer -> lecturer.getCourses().stream().anyMatch(course -> course.getCourseCode().equalsIgnoreCase(courseCode)))
                .orElse(false);
        if (!assignedCourse) throw new IllegalArgumentException("Course is not assigned to this lecturer");
        LectureHall hall = lectureHallRepository.findByName(lectureHallName)
                .orElseThrow(() -> new IllegalArgumentException("Lecture hall not found: " + lectureHallName));
        Instant scheduledStart = ZonedDateTime.of(date, startTime, ZoneId.of("Asia/Colombo")).toInstant();
        Instant now = Instant.now();
        List<AttendanceSession> activeSessions = sessionRepository.findByLecturerNameAndActiveTrue(lecturerName);
        activeSessions.forEach(activeSession -> endIfExpired(activeSession, now));
        activeSessions.stream().filter(AttendanceSession::isActive).forEach(activeSession -> {
            activeSession.setActive(false);
            activeSession.setEndedAt(now);
        });
        if (!activeSessions.isEmpty()) sessionRepository.saveAll(activeSessions);
        AttendanceSession session = new AttendanceSession();
        session.setCourseCode(courseCode); session.setLecturerName(lecturerName); session.setLectureHall(hall);
        byte[] secretKey = new byte[32];
        new SecureRandom().nextBytes(secretKey);
        session.setSecretKey(secretKey); session.setScheduledDate(date); session.setScheduledStartTime(startTime);
        session.setDurationMinutes(durationMinutes); session.setStartedAt(scheduledStart);
        Instant scheduledEnd = scheduledStart.plusSeconds(durationMinutes * 60L);
        boolean active = !scheduledStart.isAfter(now) && scheduledEnd.isAfter(now);
        session.setActive(active);
        if (!active && !scheduledStart.isAfter(now)) session.setEndedAt(scheduledEnd);
        return sessionRepository.saveAndFlush(session);
    }

    @Transactional
    public AttendanceSession stopSession(UUID sessionId, String lecturerUsername) {
        AttendanceSession session = getOwnedSession(sessionId, lecturerUsername);
        Instant now = Instant.now();
        activateIfScheduled(session, now);
        endIfExpired(session, now);
        if (!session.isActive()) return session;
        session.setActive(false); session.setEndedAt(Instant.now());
        return sessionRepository.save(session);
    }

    @Transactional
    public AttendanceRecord recordAttendance(UUID sessionId, String authenticatedUsername, String status) {
        AttendanceSession session = getSession(sessionId);
        ensureSessionActive(session);
        Student student = studentRepository.findByUserUsername(authenticatedUsername)
                .orElseThrow(StudentNotEnrolledException::new);
        if (recordRepository.existsBySessionIdAndStudentId(sessionId, student.getStudentId())) {
            throw new AttendanceAlreadyMarkedException();
        }

        AttendanceRecord record = new AttendanceRecord(); record.setSession(session); record.setStudentId(student.getStudentId());
        record.setStatus(status == null ? "PRESENT" : status);
        try {
            return recordRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateAttendanceConstraint(exception)) throw new AttendanceAlreadyMarkedException();
            throw exception;
        }
    }

    @Transactional
    public AttendanceRecord checkIn(String qrToken, String authenticatedUsername) {
        UUID sessionId = qrTokenService.validateToken(qrToken);
        return recordAttendance(sessionId, authenticatedUsername, "PRESENT");
    }

    @Transactional
    public QrChallengeResponse getQrChallenge(UUID sessionId, String lecturerUsername) {
        AttendanceSession session = getOwnedSession(sessionId, lecturerUsername);
        ensureSessionActive(session);
        Instant serverTime = Instant.now();
        // Start each challenge's validity period at issue time. Aligning it to a
        // global wall-clock window can make a newly started session's first QR
        // code expire almost immediately.
        Instant expiresAt = qrTokenService.expiryFrom(serverTime);
        long windowStart = serverTime.getEpochSecond();
        return new QrChallengeResponse(qrTokenService.generateToken(sessionId, expiresAt),
                qrTokenService.generateCode(sessionId, windowStart), expiresAt, serverTime);
    }

    @Transactional
    public AttendanceRecord checkInWithCode(UUID sessionId, String code, String authenticatedUsername) {
        if (!qrTokenService.validateCode(sessionId, code, Instant.now())) {
            throw new IllegalArgumentException("Attendance code is invalid or expired");
        }
        return recordAttendance(sessionId, authenticatedUsername, "PRESENT");
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getRecords(UUID sessionId, String lecturerUsername) {
        getOwnedSession(sessionId, lecturerUsername);
        return recordRepository.findBySessionIdOrderByCheckedInAtDesc(sessionId);
    }

    @Transactional
    public Optional<AttendanceSession> getLatestSession(String lecturerUsername) {
        Optional<AttendanceSession> latest = sessionRepository.findFirstByLecturerNameOrderByStartedAtDesc(lecturerUsername);
        latest.ifPresent(session -> {
            Instant now = Instant.now();
            activateIfScheduled(session, now);
            endIfExpired(session, now);
        });
        return latest;
    }

    @Transactional
    public LecturerDashboardResponse getLecturerDashboard(String lecturerName) {
        List<AttendanceSession> sessions = sessionRepository.findByLecturerNameOrderByStartedAtDesc(lecturerName);
        Instant now = Instant.now();
        sessions.forEach(session -> {
            activateIfScheduled(session, now);
            endIfExpired(session, now);
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
                        entry.getValue().checkIns, entry.getValue().latestSessionAt))
                .toList();
        List<LecturerDashboardResponse.SessionSummary> recentSessions = sessions.stream().limit(5)
                .map(session -> new LecturerDashboardResponse.SessionSummary(session.getId().toString(),
                        session.getCourseCode(), session.getStartedAt(), session.isActive(),
                        checkInsBySession.getOrDefault(session.getId(), 0L)))
                .toList();

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

    private AttendanceSession getOwnedSession(UUID sessionId, String lecturerUsername) {
        return sessionRepository.findByIdAndLecturerName(sessionId, lecturerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));
    }

    private void ensureSessionActive(AttendanceSession session) {
        Instant now = Instant.now();
        activateIfScheduled(session, now);
        endIfExpired(session, now);
        if (!session.isActive()) throw new IllegalStateException("Attendance session is not active");
    }

    private void activateIfScheduled(AttendanceSession session, Instant now) {
        if (!session.isActive() && session.getEndedAt() == null && !session.getStartedAt().isAfter(now)) {
            session.setActive(true);
            sessionRepository.save(session);
        }
    }

    private void endIfExpired(AttendanceSession session, Instant now) {
        if (session.isActive() && !session.getStartedAt().plusSeconds(session.getDurationMinutes() * 60L).isAfter(now)) {
            session.setActive(false);
            session.setEndedAt(session.getStartedAt().plusSeconds(session.getDurationMinutes() * 60L));
            sessionRepository.save(session);
        }
    }

    private boolean isDuplicateAttendanceConstraint(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return "uq_student_session".equalsIgnoreCase(violation.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public AttendanceSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));
    }
}
