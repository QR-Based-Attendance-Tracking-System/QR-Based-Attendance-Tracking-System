package lk.ruhunaefac.qrattendance.attendance.repository;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findBySessionId(UUID sessionId);
}
