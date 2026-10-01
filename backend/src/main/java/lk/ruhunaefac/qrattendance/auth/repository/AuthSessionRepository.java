package lk.ruhunaefac.qrattendance.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);
    void deleteByTokenHash(String tokenHash);
    void deleteByExpiresAtBefore(Instant now);
}
