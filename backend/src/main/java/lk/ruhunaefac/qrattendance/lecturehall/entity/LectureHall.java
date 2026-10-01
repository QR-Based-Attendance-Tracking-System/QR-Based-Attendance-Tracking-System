package lk.ruhunaefac.qrattendance.lecturehall.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name = "lecture_halls")
public class LectureHall {
    public LectureHall() { }
    @Id @GeneratedValue @UuidGenerator private UUID id;
    @Column(nullable = false, length = 150) private String name;
    private Double latitude;
    private Double longitude;
    @Column(name = "geofence_radius_meters") private Double geofenceRadiusMeters;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getGeofenceRadiusMeters() { return geofenceRadiusMeters; }
    public void setGeofenceRadiusMeters(Double geofenceRadiusMeters) { this.geofenceRadiusMeters = geofenceRadiusMeters; }
    public Instant getCreatedAt() { return createdAt; }
}
