package lk.ruhunaefac.qrattendance.attendance.repository;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {
}
