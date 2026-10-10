package lk.ruhunaefac.qrattendance.attendance.service;

import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.attendance.session.repository.AttendanceSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Shared session lookup, ownership, and scheduled/expiry state rules. */
@Service
public class AttendanceSessionLifecycleService {
    private final AttendanceSessionRepository sessionRepository;

    public AttendanceSessionLifecycleService(AttendanceSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public AttendanceSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));
    }

    @Transactional(readOnly = true)
    public AttendanceSession getOwnedSession(UUID sessionId, String lecturerUsername) {
        return sessionRepository.findByIdAndLecturerName(sessionId, lecturerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Attendance session not found"));
    }

    public void ensureActive(AttendanceSession session) {
        Instant now = Instant.now();
        activateIfScheduled(session, now);
        endIfExpired(session, now);
        if (!session.isActive()) throw new IllegalStateException("Attendance session is not active");
    }

    public void activateIfScheduled(AttendanceSession session, Instant now) {
        if (!session.isActive() && session.getEndedAt() == null && !session.getStartedAt().isAfter(now)) {
            session.setActive(true);
            if (session.getQrWindowStartedAt() == null) session.setQrWindowStartedAt(now);
            sessionRepository.save(session);
        }
    }

    public void endIfExpired(AttendanceSession session, Instant now) {
        if (session.isActive() && !session.getStartedAt()
                .plusSeconds(session.getDurationMinutes() * 60L).isAfter(now)) {
            session.setActive(false);
            session.setEndedAt(session.getStartedAt().plusSeconds(session.getDurationMinutes() * 60L));
            sessionRepository.save(session);
        }
    }
}
