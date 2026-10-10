package lk.ruhunaefac.qrattendance.attendance.challenge.service;

import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.challenge.dto.QrChallengeResponse;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;
import lk.ruhunaefac.qrattendance.attendance.service.AttendanceSessionLifecycleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QrChallengeService {
    private final QrTokenService qrTokenService;
    private final AttendanceSessionLifecycleService lifecycle;

    public QrChallengeService(QrTokenService qrTokenService, AttendanceSessionLifecycleService lifecycle) {
        this.qrTokenService = qrTokenService;
        this.lifecycle = lifecycle;
    }

    @Transactional
    public QrChallengeResponse createChallenge(UUID sessionId, String lecturerUsername) {
        AttendanceSession session = lifecycle.getOwnedSession(sessionId, lecturerUsername);
        lifecycle.ensureActive(session);
        Instant serverTime = Instant.now();
        Instant windowStart = qrTokenService.currentWindowStart(session.getQrWindowStartedAt(), serverTime);
        Instant expiresAt = qrTokenService.windowExpiry(windowStart);
        return new QrChallengeResponse(qrTokenService.generateToken(sessionId, expiresAt),
                qrTokenService.generateCode(sessionId, windowStart), expiresAt, serverTime);
    }
}
