package lk.ruhunaefac.qrattendance.qr.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QrTokenService {
    private static final String HMAC = "HmacSHA256";
    private final byte[] signingKey;
    private final long validitySeconds;

    public QrTokenService(@Value("${attendance.qr-token-secret}") String secret, @Value("${attendance.qr-token-validity-seconds:30}") long validitySeconds) {
        if (secret == null || secret.isBlank()) throw new IllegalArgumentException("QR token secret must be configured");
        if (validitySeconds <= 0) throw new IllegalArgumentException("QR token validity must be greater than zero");
        this.signingKey = secret.getBytes(StandardCharsets.UTF_8); this.validitySeconds = validitySeconds;
    }

    public String generateToken(UUID sessionId) {
        String payload = sessionId + "." + Instant.now().plusSeconds(validitySeconds).toEpochMilli();
        return encodeToken(payload);
    }

    public String generateToken(UUID sessionId, Instant expiresAt) {
        String payload = sessionId + "." + expiresAt.toEpochMilli();
        return encodeToken(payload);
    }

    private String encodeToken(String payload) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + sign(payload);
    }

    public Instant currentWindowStart(Instant sessionStartedAt, Instant now) {
        if (now.isBefore(sessionStartedAt)) throw new IllegalArgumentException("Attendance session has not started");
        long windowMillis = validitySeconds * 1000L;
        long elapsedMillis = Duration.between(sessionStartedAt, now).toMillis();
        long windowIndex = Math.floorDiv(elapsedMillis, windowMillis);
        return sessionStartedAt.plusMillis(windowIndex * windowMillis);
    }

    public long validitySeconds() {
        return validitySeconds;
    }

    public Instant windowExpiry(Instant windowStart) {
        return windowStart.plusSeconds(validitySeconds);
    }

    /** Derives an unpredictable, six digit code from the server-only signing key and current window. */
    public String generateCode(UUID sessionId, Instant windowStart) {
        byte[] digest = signBytes("attendance-code:" + sessionId + ":" + windowStart.toEpochMilli());
        long value = Integer.toUnsignedLong(java.nio.ByteBuffer.wrap(digest).getInt());
        return String.format(Locale.ROOT, "%06d", value % 1_000_000);
    }

    public boolean validateCode(UUID sessionId, Instant sessionStartedAt, String code, Instant now) {
        if (code == null || !code.matches("\\d{6}")) return false;
        String expected = generateCode(sessionId, currentWindowStart(sessionStartedAt, now));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), code.getBytes(StandardCharsets.US_ASCII));
    }

    /** Reads the session ID only to locate its persisted start time; callers must still validate the signature. */
    public UUID readSessionId(String token) {
        try {
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Malformed QR token");
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\.", 2);
            if (values.length != 2) throw new IllegalArgumentException("Malformed QR token payload");
            return UUID.fromString(values[0]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid QR token", ex);
        }
    }

    public UUID validateToken(String token, Instant sessionStartedAt, Instant now) {
        try {
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Malformed QR token");
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\.", 2);
            if (values.length != 2) throw new IllegalArgumentException("Malformed QR token payload");
            if (!MessageDigest.isEqual(parts[1].getBytes(StandardCharsets.UTF_8), sign(payload).getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException("Invalid QR token signature");
            long expiresAt = Long.parseLong(values[1]);
            Instant currentWindowExpiry = windowExpiry(currentWindowStart(sessionStartedAt, now));
            if (expiresAt != currentWindowExpiry.toEpochMilli()) throw new IllegalArgumentException("QR token has expired or is not current");
            return UUID.fromString(values[0]);
        } catch (RuntimeException ex) { throw new IllegalArgumentException("Invalid QR token", ex); }
    }

    private String sign(String payload) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signBytes(payload));
    }

    private byte[] signBytes(String payload) {
        try { Mac mac = Mac.getInstance(HMAC); mac.init(new SecretKeySpec(signingKey, HMAC)); return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception ex) { throw new IllegalStateException("Unable to sign QR token", ex); }
    }
}
