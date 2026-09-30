package lk.ruhunaefac.qrattendance.attendance.service;

import java.time.Instant;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.List;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceRecord;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.attendance.repository.AttendanceRecordRepository;
import lk.ruhunaefac.qrattendance.attendance.repository.AttendanceSessionRepository;
import lk.ruhunaefac.qrattendance.lecturehall.entity.LectureHall;
import lk.ruhunaefac.qrattendance.lecturehall.repository.LectureHallRepository;
import lk.ruhunaefac.qrattendance.qr.service.QrTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final LectureHallRepository lectureHallRepository;
    private final QrTokenService qrTokenService;

    public AttendanceService(AttendanceSessionRepository sessionRepository, AttendanceRecordRepository recordRepository, LectureHallRepository lectureHallRepository, QrTokenService qrTokenService) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.lectureHallRepository = lectureHallRepository;
        this.qrTokenService = qrTokenService;
    }

    @Transactional
    public AttendanceSession startSession(String courseCode, String lecturerName, UUID lectureHallId) {
        byte[] secretKey = new byte[32];
        new SecureRandom().nextBytes(secretKey);
        return startSession(courseCode, lecturerName, lectureHallId, secretKey);
    }

    @Transactional
    public AttendanceSession startSession(String courseCode, String lecturerName, UUID lectureHallId, byte[] secretKey) {
        LectureHall hall = lectureHallRepository.findById(lectureHallId).orElseThrow(() -> new IllegalArgumentException("Lecture hall not found: " + lectureHallId));
        AttendanceSession session = new AttendanceSession();
        session.setCourseCode(courseCode); session.setLecturerName(lecturerName); session.setLectureHall(hall);
        session.setSecretKey(secretKey.clone()); session.setStartedAt(Instant.now()); session.setActive(true);
        return sessionRepository.save(session);
    }

    @Transactional
    public AttendanceSession stopSession(UUID sessionId) {
        AttendanceSession session = getSession(sessionId);
        if (!session.isActive()) return session;
        session.setActive(false); session.setEndedAt(Instant.now());
        return sessionRepository.save(session);
    }

    @Transactional
    public AttendanceRecord recordAttendance(UUID sessionId, String studentId, String status) {
        AttendanceSession session = getSession(sessionId);
        if (!session.isActive()) throw new IllegalStateException("Attendance session is not active");
        AttendanceRecord record = new AttendanceRecord(); record.setSession(session); record.setStudentId(studentId);
        record.setStatus(status == null ? "PRESENT" : status);
        return recordRepository.save(record);
    }

    @Transactional
    public AttendanceRecord checkIn(String qrToken, String authenticatedStudentId) {
        UUID sessionId = qrTokenService.validateToken(qrToken);
        return recordAttendance(sessionId, authenticatedStudentId, "PRESENT");
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getRecords(UUID sessionId) {
        getSession(sessionId);
        return recordRepository.findBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public AttendanceSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));
    }
}
