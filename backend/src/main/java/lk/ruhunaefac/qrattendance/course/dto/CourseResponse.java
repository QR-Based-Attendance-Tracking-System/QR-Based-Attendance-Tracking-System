package lk.ruhunaefac.qrattendance.course.dto;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.course.entity.Course;

public record CourseResponse(UUID id, String courseCode, String courseName) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getCourseCode(), course.getCourseName());
    }
}
