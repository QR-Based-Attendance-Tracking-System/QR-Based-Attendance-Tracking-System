package lk.ruhunaefac.qrattendance.user.entity;

import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "admins")
public class Admin {
    public Admin() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "admin_id", nullable = false, unique = true, length = 100) private String adminId;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
