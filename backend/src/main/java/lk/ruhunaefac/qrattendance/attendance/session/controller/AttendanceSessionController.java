package lk.ruhunaefac.qrattendance.attendance.session.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.session.dto.AttendanceSessionResponse;
import lk.ruhunaefac.qrattendance.attendance.session.dto.StartAttendanceSessionRequest;
import lk.ruhunaefac.qrattendance.attendance.session.service.AttendanceSessionService;
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
public class AttendanceSessionController {
    private final AttendanceSessionService attendanceSessionService;

    public AttendanceSessionController(AttendanceSessionService attendanceSessionService) {
        this.attendanceSessionService = attendanceSessionService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionResponse> startSession(@Valid @RequestBody StartAttendanceSessionRequest request,
                                                                   @AuthenticationPrincipal UserDetails principal) {
        try {
            String lecturerName = principal.getUsername();
            var session = attendanceSessionService.startSession(request.courseCode(), lecturerName,
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
        return AttendanceSessionResponse.from(attendanceSessionService.stopSession(sessionId, principal.getUsername()));
    }

    @GetMapping("/sessions/latest")
    public ResponseEntity<AttendanceSessionResponse> getLatestSession(@AuthenticationPrincipal UserDetails principal) {
        Optional<AttendanceSessionResponse> latest = attendanceSessionService.getLatestSession(principal.getUsername()).map(AttendanceSessionResponse::from);
        return latest.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<AttendanceSessionResponse> getActiveSession(@AuthenticationPrincipal UserDetails principal) {
        Optional<AttendanceSessionResponse> active = attendanceSessionService.getActiveSession(principal.getUsername()).map(AttendanceSessionResponse::from);
        return active.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

}
