package lk.ruhunaefac.qrattendance.qr.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public QrTokenService(@Value("${attendance.qr-token-secret}") String secret, @Value("${attendance.qr-token-validity-seconds:60}") long validitySeconds) {
        if (secret == null || secret.isBlank()) throw new IllegalArgumentException("QR token secret must be configured");
        if (validitySeconds <= 0) throw new IllegalArgumentException("QR token validity must be greater than zero");
        this.signingKey = secret.getBytes(StandardCharsets.UTF_8); this.validitySeconds = validitySeconds;
    }

    public String generateToken(UUID sessionId) {
        String payload = sessionId + "." + Instant.now().plusSeconds(validitySeconds).getEpochSecond();
        return encodeToken(payload);
    }

    public String generateToken(UUID sessionId, Instant expiresAt) {
        String payload = sessionId + "." + expiresAt.getEpochSecond();
        return encodeToken(payload);
    }

    private String encodeToken(String payload) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + sign(payload);
    }

    public Instant currentWindowExpiry(Instant now) {
        long window = Math.floorDiv(now.getEpochSecond(), validitySeconds);
        return Instant.ofEpochSecond(Math.multiplyExact(window + 1, validitySeconds));
    }

    public long currentWindowStart(Instant now) {
        return Math.floorDiv(now.getEpochSecond(), validitySeconds) * validitySeconds;
    }

    /** Derives an unpredictable, six digit code from the server-only signing key and current window. */
    public String generateCode(UUID sessionId, long windowStart) {
        byte[] digest = signBytes("attendance-code:" + sessionId + ":" + windowStart);
        long value = Integer.toUnsignedLong(java.nio.ByteBuffer.wrap(digest).getInt());
        return String.format(Locale.ROOT, "%06d", value % 1_000_000);
    }

    public boolean validateCode(UUID sessionId, String code, Instant now) {
        if (code == null || !code.matches("\\d{6}")) return false;
        String expected = generateCode(sessionId, currentWindowStart(now));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), code.getBytes(StandardCharsets.US_ASCII));
    }

    public UUID validateToken(String token) {
        try {
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Malformed QR token");
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\.", 2);
            if (!MessageDigest.isEqual(parts[1].getBytes(StandardCharsets.UTF_8), sign(payload).getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException("Invalid QR token signature");
            if (Instant.now().getEpochSecond() >= Long.parseLong(values[1])) throw new IllegalArgumentException("QR token has expired");
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
