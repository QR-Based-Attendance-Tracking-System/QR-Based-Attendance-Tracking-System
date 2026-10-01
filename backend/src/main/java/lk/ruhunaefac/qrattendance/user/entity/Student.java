package lk.ruhunaefac.qrattendance.user.entity;

import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "students")
public class Student {
    public Student() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "student_id", nullable = false, unique = true, length = 100) private String studentId;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    @Enumerated(EnumType.STRING) @Column(name = "department", length = 20) private Department department;
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public enum Department { ELEC, COM, MENA, MECH, CIVIL }
}
