package lk.ruhunaefac.qrattendance.user.entity;

import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "lecturers")
public class Lecturer {
    public Lecturer() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "lecturer_id", nullable = false, unique = true, length = 100) private String lecturerId;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
