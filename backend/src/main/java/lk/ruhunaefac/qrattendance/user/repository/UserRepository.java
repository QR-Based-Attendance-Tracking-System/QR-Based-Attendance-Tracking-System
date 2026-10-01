package lk.ruhunaefac.qrattendance.user.repository;

import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}
