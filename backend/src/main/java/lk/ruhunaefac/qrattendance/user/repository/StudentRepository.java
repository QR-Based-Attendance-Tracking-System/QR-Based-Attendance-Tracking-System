package lk.ruhunaefac.qrattendance.user.repository;

import java.util.UUID;
import java.util.Optional;
import lk.ruhunaefac.qrattendance.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserUsername(String username);
}
