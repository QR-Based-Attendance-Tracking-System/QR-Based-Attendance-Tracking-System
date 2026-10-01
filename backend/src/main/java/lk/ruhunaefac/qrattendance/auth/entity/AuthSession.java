package lk.ruhunaefac.qrattendance.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.user.entity.User;

@Entity
@Table(name = "auth_sessions")
public class AuthSession {
    @Id private UUID id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    protected AuthSession() { }
    public AuthSession(UUID id, User user, String tokenHash, Instant now, Instant expiresAt) {
        this.id = id; this.user = user; this.tokenHash = tokenHash;
        this.createdAt = now; this.expiresAt = expiresAt;
    }
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
}
