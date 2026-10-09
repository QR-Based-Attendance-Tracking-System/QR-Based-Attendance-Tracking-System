package lk.ruhunaefac.qrattendance.attendance.controller;

import jakarta.validation.Valid;
import java.util.function.Supplier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.dto.AttendanceRecordResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.AttendanceSessionResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.CheckInRequest;
import lk.ruhunaefac.qrattendance.attendance.dto.CodeCheckInRequest;
import lk.ruhunaefac.qrattendance.attendance.dto.QrChallengeResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.LecturerDashboardResponse;
import lk.ruhunaefac.qrattendance.attendance.dto.StartAttendanceSessionRequest;
import lk.ruhunaefac.qrattendance.attendance.entity.AttendanceRecord;
import lk.ruhunaefac.qrattendance.attendance.exception.AttendanceAlreadyMarkedException;
import lk.ruhunaefac.qrattendance.attendance.exception.StudentNotEnrolledException;
import lk.ruhunaefac.qrattendance.attendance.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
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
    public ResponseEntity<AttendanceSessionResponse> startSession(@Valid @RequestBody StartAttendanceSessionRequest request,
                                                                   @AuthenticationPrincipal UserDetails principal) {
        try {
            String lecturerName = principal.getUsername();
            var session = attendanceService.startSession(request.courseCode(), lecturerName,
                    request.lectureHallName(), request.date(), request.startTime(), request.durationMinutes());
            return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceSessionResponse.from(session));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    @PostMapping("/sessions/{sessionId}/end")
    public AttendanceSessionResponse endSession(@PathVariable UUID sessionId, @AuthenticationPrincipal UserDetails principal) {
        return AttendanceSessionResponse.from(attendanceService.stopSession(sessionId, principal.getUsername()));
    }

    @GetMapping("/sessions/latest")
    public ResponseEntity<AttendanceSessionResponse> getLatestSession(@AuthenticationPrincipal UserDetails principal) {
        Optional<AttendanceSessionResponse> latest = attendanceService.getLatestSession(principal.getUsername()).map(AttendanceSessionResponse::from);
        return latest.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<AttendanceSessionResponse> getActiveSession(@AuthenticationPrincipal UserDetails principal) {
        Optional<AttendanceSessionResponse> active = attendanceService.getActiveSession(principal.getUsername()).map(AttendanceSessionResponse::from);
        return active.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/lecturer-dashboard")
    public LecturerDashboardResponse getLecturerDashboard(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return attendanceService.getLecturerDashboard(principal.getUsername());
    }

    @GetMapping("/sessions/{sessionId}/qr-challenge")
    public ResponseEntity<QrChallengeResponse> getQrChallenge(@PathVariable UUID sessionId, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(attendanceService.getQrChallenge(sessionId, principal.getUsername()));
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecordResponse> checkIn(@Valid @RequestBody CheckInRequest request,
                                                            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return createCheckIn(() -> attendanceService.checkIn(request.qrToken(), principal.getUsername()));
    }

    @PostMapping("/check-in/code")
    public ResponseEntity<AttendanceRecordResponse> checkInWithCode(@Valid @RequestBody CodeCheckInRequest request,
                                                                     @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return createCheckIn(() -> attendanceService.checkInWithCode(request.sessionId(), request.code(), principal.getUsername()));
    }

    @GetMapping("/sessions/{sessionId}/records")
    public List<AttendanceRecordResponse> getRecords(@PathVariable UUID sessionId, @AuthenticationPrincipal UserDetails principal) {
        return attendanceService.getRecords(sessionId, principal.getUsername()).stream().map(AttendanceRecordResponse::from).toList();
    }

    private ResponseEntity<AttendanceRecordResponse> createCheckIn(Supplier<AttendanceRecord> checkIn) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceRecordResponse.from(checkIn.get()));
        } catch (AttendanceAlreadyMarkedException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        } catch (StudentNotEnrolledException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
