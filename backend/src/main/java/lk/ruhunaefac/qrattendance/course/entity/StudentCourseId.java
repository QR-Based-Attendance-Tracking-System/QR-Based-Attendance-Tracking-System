package lk.ruhunaefac.qrattendance.course.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class StudentCourseId implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID student;
    private UUID course;

    public StudentCourseId() { }

    public StudentCourseId(UUID student, UUID course) {
        this.student = student;
        this.course = course;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof StudentCourseId that)) return false;
        return Objects.equals(student, that.student) && Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() { return Objects.hash(student, course); }
}
