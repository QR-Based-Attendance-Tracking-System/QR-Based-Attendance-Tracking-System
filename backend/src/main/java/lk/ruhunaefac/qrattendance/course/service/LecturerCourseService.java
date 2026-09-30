package lk.ruhunaefac.qrattendance.course.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import lk.ruhunaefac.qrattendance.course.dto.AddCourseRequest;
import lk.ruhunaefac.qrattendance.course.dto.CourseResponse;
import lk.ruhunaefac.qrattendance.course.entity.Course;
import lk.ruhunaefac.qrattendance.course.repository.CourseRepository;
import lk.ruhunaefac.qrattendance.user.entity.Lecturer;
import lk.ruhunaefac.qrattendance.user.repository.LecturerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LecturerCourseService {
    private final LecturerRepository lecturerRepository;
    private final CourseRepository courseRepository;

    public LecturerCourseService(LecturerRepository lecturerRepository, CourseRepository courseRepository) {
        this.lecturerRepository = lecturerRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses(String username) {
        Lecturer lecturer = getLecturer(username);
        return lecturer.getCourses().stream()
                .sorted(Comparator.comparing(Course::getCourseCode))
                .map(CourseResponse::from)
                .toList();
    }

    @Transactional
    public CourseResponse addCourse(String username, AddCourseRequest request) {
        Lecturer lecturer = getLecturer(username);
        String courseCode = request.courseCode().strip().toUpperCase(Locale.ROOT);
        String courseName = request.courseName().strip();
        Course course = courseRepository.findByCourseCodeIgnoreCase(courseCode).orElseGet(() -> {
            Course newCourse = new Course();
            newCourse.setCourseCode(courseCode);
            newCourse.setCourseName(courseName);
            return courseRepository.save(newCourse);
        });

        boolean alreadyAssigned = lecturer.getCourses().stream()
                .anyMatch(assignedCourse -> assignedCourse.getId().equals(course.getId()));
        if (!alreadyAssigned) {
            lecturer.getCourses().add(course);
            lecturerRepository.save(lecturer);
        }
        return CourseResponse.from(course);
    }

    @Transactional
    public void removeCourse(String username, UUID courseId) {
        Lecturer lecturer = getLecturer(username);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));
        lecturer.getCourses().removeIf(assignedCourse -> assignedCourse.getId().equals(course.getId()));
        lecturerRepository.save(lecturer);
    }

    private Lecturer getLecturer(String username) {
        return lecturerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("No lecturer profile is linked to the authenticated user"));
    }
}
