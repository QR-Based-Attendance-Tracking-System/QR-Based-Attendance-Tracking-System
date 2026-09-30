package lk.ruhunaefac.qrattendance.user.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.course.entity.Course;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "lecturers")
public class Lecturer {
    public Lecturer() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "lecturer_id", nullable = false, unique = true, length = 100) private String lecturerId;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    @ManyToMany
    @JoinTable(name = "lecturer_courses",
            joinColumns = @JoinColumn(name = "lecturer_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> courses = new ArrayList<>();
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public List<Course> getCourses() { return courses; }
}
