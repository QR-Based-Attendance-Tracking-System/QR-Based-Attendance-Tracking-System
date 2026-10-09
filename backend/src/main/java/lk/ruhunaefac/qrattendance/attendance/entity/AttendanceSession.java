package lk.ruhunaefac.qrattendance.attendance.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.lecturehall.entity.LectureHall;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Entity @Table(name = "attendance_sessions")
public class AttendanceSession {
    public AttendanceSession() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @Column(name = "course_code", nullable = false, length = 50) private String courseCode;
    @Column(name = "lecturer_name", nullable = false, length = 150) private String lecturerName;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "lecture_hall_id", nullable = false)
    private LectureHall lectureHall;
    @JdbcTypeCode(SqlTypes.BINARY) @Column(name = "secret_key", nullable = false, columnDefinition = "bytea") private byte[] secretKey;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "scheduled_date", nullable = false) private LocalDate scheduledDate;
    @Column(name = "scheduled_start_time", nullable = false) private LocalTime scheduledStartTime;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(name = "ended_at") private Instant endedAt;
    @Column(nullable = false) private boolean active = true;
    public UUID getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }
    public LectureHall getLectureHall() { return lectureHall; }
    public void setLectureHall(LectureHall lectureHall) { this.lectureHall = lectureHall; }
    public byte[] getSecretKey() { return secretKey; }
    public void setSecretKey(byte[] secretKey) { this.secretKey = secretKey; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
