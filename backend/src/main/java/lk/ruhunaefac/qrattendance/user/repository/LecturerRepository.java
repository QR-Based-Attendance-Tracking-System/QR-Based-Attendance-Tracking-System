package lk.ruhunaefac.qrattendance.user.repository;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.user.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {
}
