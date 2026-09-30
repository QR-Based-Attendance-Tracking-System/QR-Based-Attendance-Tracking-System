package lk.ruhunaefac.qrattendance.attendance.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.dto.AttendanceRecordResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.AttendanceSessionResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.CheckInRequest;
import lk.ruhunaefac.qrattendance.attendance.dto.StartAttendanceSessionRequest;
import lk.ruhunaefac.qrattendance.attendance.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionResponse> startSession(@Valid @RequestBody StartAttendanceSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceSessionResponse.from(
                attendanceService.startSession(request.courseCode(), request.lecturerName(), request.lectureHallId())));
    }

    @PostMapping("/sessions/{sessionId}/end")
    public AttendanceSessionResponse endSession(@PathVariable UUID sessionId) {
        return AttendanceSessionResponse.from(attendanceService.stopSession(sessionId));
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecordResponse> checkIn(@Valid @RequestBody CheckInRequest request,
                                                            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceRecordResponse.from(
                attendanceService.checkIn(request.qrToken(), principal.getUsername())));
    }

    @GetMapping("/sessions/{sessionId}/records")
    public List<AttendanceRecordResponse> getRecords(@PathVariable UUID sessionId) {
        return attendanceService.getRecords(sessionId).stream().map(AttendanceRecordResponse::from).toList();
    }
}
