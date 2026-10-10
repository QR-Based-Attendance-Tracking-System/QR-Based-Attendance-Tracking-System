package lk.ruhunaefac.qrattendance.attendance.checkin.service;

import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.challenge.service.QrTokenService;
import lk.ruhunaefac.qrattendance.attendance.checkin.entity.AttendanceRecord;
import lk.ruhunaefac.qrattendance.attendance.checkin.exception.AttendanceAlreadyMarkedException;
import lk.ruhunaefac.qrattendance.attendance.checkin.exception.StudentNotEnrolledException;
import lk.ruhunaefac.qrattendance.attendance.checkin.repository.AttendanceRecordRepository;
import lk.ruhunaefac.qrattendance.attendance.service.AttendanceSessionLifecycleService;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.user.entity.Student;
import lk.ruhunaefac.qrattendance.user.repository.StudentRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceCheckInService {
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final QrTokenService qrTokenService;
    private final AttendanceSessionLifecycleService lifecycle;

    public AttendanceCheckInService(AttendanceRecordRepository recordRepository, StudentRepository studentRepository,
                                    QrTokenService qrTokenService, AttendanceSessionLifecycleService lifecycle) {
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.qrTokenService = qrTokenService;
        this.lifecycle = lifecycle;
    }

    @Transactional
    public AttendanceRecord checkIn(String qrToken, String authenticatedUsername) {
        Instant now = Instant.now();
        UUID tokenSessionId = qrTokenService.readSessionId(qrToken);
        AttendanceSession session = lifecycle.getSession(tokenSessionId);
        UUID sessionId = qrTokenService.validateToken(qrToken, session.getQrWindowStartedAt(), now);
        lifecycle.ensureActive(session);
        return recordAttendance(session, sessionId, authenticatedUsername, "PRESENT");
    }

    @Transactional
    public AttendanceRecord checkInWithCode(UUID sessionId, String code, String authenticatedUsername) {
        AttendanceSession session = lifecycle.getSession(sessionId);
        lifecycle.ensureActive(session);
        if (!qrTokenService.validateCode(sessionId, session.getQrWindowStartedAt(), code, Instant.now())) {
            throw new IllegalArgumentException("Attendance code is invalid or expired");
        }
        return recordAttendance(session, sessionId, authenticatedUsername, "PRESENT");
    }

    private AttendanceRecord recordAttendance(AttendanceSession session, UUID sessionId,
                                              String authenticatedUsername, String status) {
        Student student = studentRepository.findByUserUsername(authenticatedUsername)
                .orElseThrow(StudentNotEnrolledException::new);
        if (recordRepository.existsBySessionIdAndStudentId(sessionId, student.getStudentId())) {
            throw new AttendanceAlreadyMarkedException();
        }
        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudentId(student.getStudentId());
        record.setStatus(status == null ? "PRESENT" : status);
        try {
            return recordRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateAttendanceConstraint(exception)) throw new AttendanceAlreadyMarkedException();
            throw exception;
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
}
