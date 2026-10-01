package lk.ruhunaefac.qrattendance.course.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.course.dto.AddCourseRequest;
import lk.ruhunaefac.qrattendance.course.dto.CourseResponse;
import lk.ruhunaefac.qrattendance.course.service.LecturerCourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lecturers/me/courses")
public class LecturerCourseController {
    private final LecturerCourseService lecturerCourseService;

    public LecturerCourseController(LecturerCourseService lecturerCourseService) {
        this.lecturerCourseService = lecturerCourseService;
    }

    @GetMapping
    public List<CourseResponse> getCourses(@AuthenticationPrincipal UserDetails principal) {
        try {
            return lecturerCourseService.getCourses(requireUsername(principal));
        } catch (EntityNotFoundException exception) {
            throw notFound(exception);
        }
    }

    @PostMapping
    public ResponseEntity<CourseResponse> addCourse(@AuthenticationPrincipal UserDetails principal,
                                                     @Valid @RequestBody AddCourseRequest request) {
        try {
            CourseResponse response = lecturerCourseService.addCourse(requireUsername(principal), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException exception) {
            throw notFound(exception);
        }
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> removeCourse(@AuthenticationPrincipal UserDetails principal,
                                             @PathVariable UUID courseId) {
        try {
            lecturerCourseService.removeCourse(requireUsername(principal), courseId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException exception) {
            throw notFound(exception);
        }
    }

    private ResponseStatusException notFound(EntityNotFoundException exception) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
    }

    private String requireUsername(UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return principal.getUsername();
    }
}
