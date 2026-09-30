package lk.ruhunaefac.qrattendance.attendance.exception;

public class StudentNotEnrolledException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StudentNotEnrolledException() {
        super("Student is not enrolled in the course for this attendance session");
    }
}
