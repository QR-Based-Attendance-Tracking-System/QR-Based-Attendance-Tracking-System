package lk.ruhunaefac.qrattendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.qr.service.QrTokenService;
import org.junit.jupiter.api.Test;

class QrTokenServiceTests {
    private final QrTokenService qrTokens = new QrTokenService("qr-token-test-signing-secret", 30);

    @Test
    void attendanceCodeIsValidForItsThirtySecondWindowOnly() {
        UUID sessionId = UUID.randomUUID();
        Instant issueTime = Instant.ofEpochSecond(1_000_007);
        long windowStart = qrTokens.currentWindowStart(issueTime);
        String code = qrTokens.generateCode(sessionId, windowStart);

        assertEquals(1_000_020, qrTokens.currentWindowExpiry(issueTime).getEpochSecond());
        assertTrue(qrTokens.validateCode(sessionId, code, issueTime));
        assertFalse(qrTokens.validateCode(sessionId, code, qrTokens.currentWindowExpiry(issueTime)));
    }

    @Test
    void signedQrTokenRoundTripsAndRejectsTamperingAndExpiry() {
        UUID sessionId = UUID.randomUUID();
        String validToken = qrTokens.generateToken(sessionId, Instant.now().plusSeconds(30));
        char replacement = validToken.charAt(validToken.length() - 1) == 'A' ? 'B' : 'A';

        assertEquals(sessionId, qrTokens.validateToken(validToken));
        assertThrows(IllegalArgumentException.class,
                () -> qrTokens.validateToken(validToken.substring(0, validToken.length() - 1) + replacement));

        String expiredToken = qrTokens.generateToken(sessionId, Instant.EPOCH);
        assertThrows(IllegalArgumentException.class, () -> qrTokens.validateToken(expiredToken));
    }
}
