package lk.ruhunaefac.qrattendance.course.repository;

import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);
}
