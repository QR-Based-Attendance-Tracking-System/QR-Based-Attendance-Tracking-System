package lk.ruhunaefac.qrattendance.attendance.checkin.controller;

import jakarta.validation.Valid;
import java.util.function.Supplier;
import lk.ruhunaefac.qrattendance.attendance.checkin.dto.AttendanceRecordResponse;
import lk.ruhunaefac.qrattendance.attendance.checkin.dto.CheckInRequest;
import lk.ruhunaefac.qrattendance.attendance.checkin.dto.CodeCheckInRequest;
import lk.ruhunaefac.qrattendance.attendance.checkin.entity.AttendanceRecord;
import lk.ruhunaefac.qrattendance.attendance.checkin.exception.AttendanceAlreadyMarkedException;
import lk.ruhunaefac.qrattendance.attendance.checkin.exception.StudentNotEnrolledException;
import lk.ruhunaefac.qrattendance.attendance.checkin.service.AttendanceCheckInService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceCheckInController {
    private final AttendanceCheckInService attendanceCheckInService;

    public AttendanceCheckInController(AttendanceCheckInService attendanceCheckInService) {
        this.attendanceCheckInService = attendanceCheckInService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecordResponse> checkIn(@Valid @RequestBody CheckInRequest request,
                                                            @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return createCheckIn(() -> attendanceCheckInService.checkIn(request.qrToken(), principal.getUsername()));
    }

    @PostMapping("/check-in/code")
    public ResponseEntity<AttendanceRecordResponse> checkInWithCode(@Valid @RequestBody CodeCheckInRequest request,
                                                                     @AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return createCheckIn(() -> attendanceCheckInService.checkInWithCode(request.sessionId(), request.code(), principal.getUsername()));
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
