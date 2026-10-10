package lk.ruhunaefac.qrattendance.attendance.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.Duration;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.challenge.service.QrTokenService;
import org.junit.jupiter.api.Test;

class QrTokenServiceTests {
    private final QrTokenService qrTokens = new QrTokenService("qr-token-test-signing-secret", 30);

    @Test
    void attendanceCodeIsValidForItsSessionRelativeThirtySecondWindowOnly() {
        UUID sessionId = UUID.randomUUID();
        Instant sessionStart = Instant.ofEpochMilli(1_000_007_000L);
        Instant issueTime = sessionStart.plusSeconds(7);
        Instant windowStart = qrTokens.currentWindowStart(sessionStart, issueTime);
        String code = qrTokens.generateCode(sessionId, windowStart);

        assertEquals(sessionStart.plusSeconds(30), qrTokens.windowExpiry(windowStart));
        assertTrue(qrTokens.validateCode(sessionId, sessionStart, code, issueTime));
        assertFalse(qrTokens.validateCode(sessionId, sessionStart, code, sessionStart.plusSeconds(30)));
    }

    @Test
    void challengeWindowUsesFixedServerEpochBoundaries() {
        Instant sessionStart = Instant.ofEpochMilli(1_000_007_123L);
        Instant issueTime = sessionStart.plusSeconds(7);
        Instant windowStart = qrTokens.currentWindowStart(sessionStart, issueTime);

        assertEquals(sessionStart, windowStart);
        assertEquals(sessionStart.plusSeconds(30), qrTokens.windowExpiry(windowStart));
        assertEquals(windowStart, qrTokens.currentWindowStart(sessionStart, sessionStart.plusMillis(29_999)));
        assertEquals(sessionStart.plusSeconds(30), qrTokens.currentWindowStart(sessionStart, sessionStart.plusSeconds(30)));
    }

    @Test
    void sessionsStartedAtDifferentTimesHaveIndependentWindowBoundaries() {
        Instant firstSessionStart = Instant.ofEpochMilli(1_000_007_123L);
        Instant secondSessionStart = firstSessionStart.plusSeconds(5);
        Instant now = firstSessionStart.plusSeconds(8);

        Instant firstExpiry = qrTokens.windowExpiry(qrTokens.currentWindowStart(firstSessionStart, now));
        Instant secondExpiry = qrTokens.windowExpiry(qrTokens.currentWindowStart(secondSessionStart, now));

        assertEquals(firstSessionStart.plusSeconds(30), firstExpiry);
        assertEquals(secondSessionStart.plusSeconds(30), secondExpiry);
        assertEquals(5, Duration.between(firstExpiry, secondExpiry).toSeconds());
    }

    @Test
    void signedQrTokenRoundTripsAndRejectsTamperingAndExpiry() {
        UUID sessionId = UUID.randomUUID();
        Instant sessionStart = Instant.now().minusSeconds(5);
        Instant windowStart = qrTokens.currentWindowStart(sessionStart, Instant.now());
        Instant expiry = qrTokens.windowExpiry(windowStart);
        String validToken = qrTokens.generateToken(sessionId, expiry);
        char replacement = validToken.charAt(validToken.length() - 1) == 'A' ? 'B' : 'A';

        assertEquals(sessionId, qrTokens.validateToken(validToken, sessionStart, Instant.now()));
        assertThrows(IllegalArgumentException.class,
                () -> qrTokens.validateToken(validToken.substring(0, validToken.length() - 1) + replacement, sessionStart, Instant.now()));

        String expiredToken = qrTokens.generateToken(sessionId, Instant.now().minusSeconds(1));
        assertThrows(IllegalArgumentException.class, () -> qrTokens.validateToken(expiredToken, sessionStart, Instant.now()));
    }

    @Test
    void rejectsAuthenticTokenFromPreviousWindowEvenIfItWasIssuedRecently() {
        UUID sessionId = UUID.randomUUID();
        Instant sessionStart = Instant.now().minusSeconds(100);
        Instant now = Instant.now();
        Instant currentStart = qrTokens.currentWindowStart(sessionStart, now);
        String staleToken = qrTokens.generateToken(sessionId, currentStart);
        Instant nextWindowStart = currentStart.plusSeconds(qrTokens.validitySeconds());

        assertThrows(IllegalArgumentException.class, () -> qrTokens.validateToken(staleToken, sessionStart, nextWindowStart));
    }
}
