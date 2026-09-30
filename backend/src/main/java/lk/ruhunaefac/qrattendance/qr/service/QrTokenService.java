package lk.ruhunaefac.qrattendance.qr.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
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
        this.signingKey = secret.getBytes(StandardCharsets.UTF_8); this.validitySeconds = validitySeconds;
    }

    public String generateToken(UUID sessionId) {
        String payload = sessionId + "." + Instant.now().plusSeconds(validitySeconds).getEpochSecond();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + sign(payload);
    }

    public UUID validateToken(String token) {
        try {
            String[] parts = token.split("\\.", 2);
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\.", 2);
            if (!MessageDigest.isEqual(parts[1].getBytes(StandardCharsets.UTF_8), sign(payload).getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException("Invalid QR token signature");
            if (Instant.now().getEpochSecond() >= Long.parseLong(values[1])) throw new IllegalArgumentException("QR token has expired");
            return UUID.fromString(values[0]);
        } catch (RuntimeException ex) { throw new IllegalArgumentException("Invalid QR token", ex); }
    }

    private String sign(String payload) {
        try { Mac mac = Mac.getInstance(HMAC); mac.init(new SecretKeySpec(signingKey, HMAC)); return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("Unable to sign QR token", ex); }
    }
}
