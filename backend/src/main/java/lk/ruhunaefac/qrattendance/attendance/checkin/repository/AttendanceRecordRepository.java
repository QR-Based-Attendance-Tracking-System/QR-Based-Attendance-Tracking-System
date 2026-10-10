package lk.ruhunaefac.qrattendance.attendance.checkin.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.checkin.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findBySessionIdOrderByCheckedInAtDesc(UUID sessionId);
    boolean existsBySessionIdAndStudentId(UUID sessionId, String studentId);

    @Query("select attendanceRecord.session.id as sessionId, count(attendanceRecord.id) as attendanceCount " +
            "from AttendanceRecord attendanceRecord where attendanceRecord.session.id in :sessionIds " +
            "group by attendanceRecord.session.id")
    List<SessionAttendanceCount> countBySessionIds(@Param("sessionIds") Collection<UUID> sessionIds);

    interface SessionAttendanceCount {
        UUID getSessionId();
        long getAttendanceCount();
    }
}
