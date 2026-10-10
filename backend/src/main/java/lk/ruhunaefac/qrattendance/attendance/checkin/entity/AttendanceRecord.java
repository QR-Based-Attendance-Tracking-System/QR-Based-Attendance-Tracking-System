package lk.ruhunaefac.qrattendance.attendance.checkin.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import lk.ruhunaefac.qrattendance.attendance.session.entity.AttendanceSession;

@Entity
@Table(name = "attendance_records", uniqueConstraints = @UniqueConstraint(name = "uq_student_session", columnNames = {"session_id", "student_id"}))
public class AttendanceRecord {
    public AttendanceRecord() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;
    @Column(name = "student_id", nullable = false, length = 100) private String studentId;
    @CreationTimestamp @Column(name = "checked_in_at", nullable = false, updatable = false) private Instant checkedInAt;
    @Column(nullable = false, length = 20) private String status = "PRESENT";
    public UUID getId() { return id; }
    public AttendanceSession getSession() { return session; }
    public void setSession(AttendanceSession session) { this.session = session; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public Instant getCheckedInAt() { return checkedInAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
