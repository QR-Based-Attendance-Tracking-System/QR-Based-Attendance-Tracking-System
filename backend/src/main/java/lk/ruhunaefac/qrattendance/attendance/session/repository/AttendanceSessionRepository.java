package lk.ruhunaefac.qrattendance.attendance.session.repository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {
    Optional<AttendanceSession> findFirstByOrderByStartedAtDesc();
    List<AttendanceSession> findByLecturerNameOrderByStartedAtDesc(String lecturerName);
    List<AttendanceSession> findByLecturerNameAndActiveTrue(String lecturerName);
    Optional<AttendanceSession> findFirstByLecturerNameOrderByStartedAtDesc(String lecturerName);
    Optional<AttendanceSession> findByIdAndLecturerName(UUID id, String lecturerName);
}
