package lk.ruhunaefac.qrattendance.course.repository;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.course.entity.StudentCourse;
import lk.ruhunaefac.qrattendance.course.entity.StudentCourseId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, StudentCourseId> {
    boolean existsByStudent_IdAndCourse_Id(UUID studentId, UUID courseId);
}
