package lk.ruhunaefac.qrattendance.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCourseRequest(
        @NotBlank @Size(max = 50) String courseCode,
        @NotBlank @Size(max = 150) String courseName) { }
