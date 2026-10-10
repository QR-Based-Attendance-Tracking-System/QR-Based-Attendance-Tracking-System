package lk.ruhunaefac.qrattendance.attendance.session.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.service.AttendanceSessionLifecycleService;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.attendance.session.repository.AttendanceSessionRepository;
import lk.ruhunaefac.qrattendance.lecturehall.entity.LectureHall;
import lk.ruhunaefac.qrattendance.lecturehall.repository.LectureHallRepository;
import lk.ruhunaefac.qrattendance.user.repository.LecturerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceSessionService {
    private final AttendanceSessionRepository sessionRepository;
    private final LectureHallRepository lectureHallRepository;
    private final LecturerRepository lecturerRepository;
    private final AttendanceSessionLifecycleService lifecycle;

    public AttendanceSessionService(AttendanceSessionRepository sessionRepository,
                                   LectureHallRepository lectureHallRepository,
                                   LecturerRepository lecturerRepository,
                                   AttendanceSessionLifecycleService lifecycle) {
        this.sessionRepository = sessionRepository;
        this.lectureHallRepository = lectureHallRepository;
        this.lecturerRepository = lecturerRepository;
        this.lifecycle = lifecycle;
    }

    @Transactional
    public AttendanceSession startSession(String courseCode, String lecturerName, String lectureHallName,
                                          LocalDate date, LocalTime startTime, int durationMinutes) {
        if (durationMinutes <= 0) throw new IllegalArgumentException("Duration must be greater than zero");
        if (date == null || startTime == null) throw new IllegalArgumentException("Session date and start time are required");
        boolean assignedCourse = lecturerRepository.findByUserUsername(lecturerName)
                .map(lecturer -> lecturer.getCourses().stream()
                        .anyMatch(course -> course.getCourseCode().equalsIgnoreCase(courseCode)))
                .orElse(false);
        if (!assignedCourse) throw new IllegalArgumentException("Course is not assigned to this lecturer");
        LectureHall hall = lectureHallRepository.findByName(lectureHallName)
                .orElseThrow(() -> new IllegalArgumentException("Lecture hall not found: " + lectureHallName));
        Instant scheduledStart = ZonedDateTime.of(date, startTime, ZoneId.of("Asia/Colombo")).toInstant();
        Instant now = Instant.now();
        List<AttendanceSession> activeSessions = sessionRepository.findByLecturerNameAndActiveTrue(lecturerName);
        activeSessions.forEach(session -> lifecycle.endIfExpired(session, now));
        activeSessions.stream().filter(AttendanceSession::isActive).forEach(session -> {
            session.setActive(false);
            session.setEndedAt(now);
        });
        if (!activeSessions.isEmpty()) sessionRepository.saveAll(activeSessions);

        AttendanceSession session = new AttendanceSession();
        session.setCourseCode(courseCode);
        session.setLecturerName(lecturerName);
        session.setLectureHall(hall);
        byte[] secretKey = new byte[32];
        new SecureRandom().nextBytes(secretKey);
        session.setSecretKey(secretKey);
        session.setScheduledDate(date);
        session.setScheduledStartTime(startTime);
        session.setDurationMinutes(durationMinutes);
        session.setStartedAt(scheduledStart);
        Instant scheduledEnd = scheduledStart.plusSeconds(durationMinutes * 60L);
        boolean active = !scheduledStart.isAfter(now) && scheduledEnd.isAfter(now);
        session.setActive(active);
        session.setQrWindowStartedAt(active ? now : null);
        if (!active && !scheduledStart.isAfter(now)) session.setEndedAt(scheduledEnd);

        try {
            return sessionRepository.saveAndFlush(session);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("An attendance session was started concurrently. Refresh and try again.", exception);
        }
    }

    @Transactional
    public AttendanceSession stopSession(UUID sessionId, String lecturerUsername) {
        AttendanceSession session = lifecycle.getOwnedSession(sessionId, lecturerUsername);
        Instant now = Instant.now();
        lifecycle.activateIfScheduled(session, now);
        lifecycle.endIfExpired(session, now);
        if (!session.isActive()) return session;
        session.setActive(false);
        session.setEndedAt(now);
        return sessionRepository.save(session);
    }

    @Transactional
    public Optional<AttendanceSession> getLatestSession(String lecturerUsername) {
        Optional<AttendanceSession> latest = sessionRepository.findFirstByLecturerNameOrderByStartedAtDesc(lecturerUsername);
        latest.ifPresent(session -> {
            Instant now = Instant.now();
            lifecycle.activateIfScheduled(session, now);
            lifecycle.endIfExpired(session, now);
        });
        return latest;
    }

    @Transactional
    public Optional<AttendanceSession> getActiveSession(String lecturerUsername) {
        List<AttendanceSession> activeSessions = sessionRepository.findByLecturerNameAndActiveTrue(lecturerUsername);
        Instant now = Instant.now();
        activeSessions.forEach(session -> lifecycle.endIfExpired(session, now));
        return activeSessions.stream().filter(AttendanceSession::isActive)
                .max(java.util.Comparator.comparing(AttendanceSession::getStartedAt));
    }
}
