package lk.ruhunaefac.qrattendance.attendance.checkin.exception;

public class AttendanceAlreadyMarkedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AttendanceAlreadyMarkedException() {
        super("Attendance has already been marked for this session");
    }
}
