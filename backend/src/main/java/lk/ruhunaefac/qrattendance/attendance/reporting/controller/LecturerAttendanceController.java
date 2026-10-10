package lk.ruhunaefac.qrattendance.attendance.reporting.controller;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.checkin.dto.AttendanceRecordResponse;
import lk.ruhunaefac.qrattendance.attendance.reporting.dto.LecturerDashboardResponse;
import lk.ruhunaefac.qrattendance.attendance.reporting.service.LecturerAttendanceReportingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class LecturerAttendanceController {
    private final LecturerAttendanceReportingService reportingService;

    public LecturerAttendanceController(LecturerAttendanceReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/lecturer-dashboard")
    public LecturerDashboardResponse getLecturerDashboard(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        return reportingService.getLecturerDashboard(principal.getUsername());
    }

    @GetMapping("/sessions/{sessionId}/records")
    public List<AttendanceRecordResponse> getRecords(@PathVariable UUID sessionId,
                                                     @AuthenticationPrincipal UserDetails principal) {
        return reportingService.getRecords(sessionId, principal.getUsername()).stream()
                .map(AttendanceRecordResponse::from).toList();
    }
}
