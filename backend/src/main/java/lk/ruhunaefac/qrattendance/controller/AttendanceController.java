package lk.ruhunaefac.qrattendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    @PostMapping("/scan")
    public ResponseEntity<?> scanAttendance(
            @RequestBody AttendanceScanRequest request
    ) {

        System.out.println("=================================");
        System.out.println("QR ATTENDANCE SCAN");
        System.out.println("Student: " + request.registrationNumber());
        System.out.println("QR Data: " + request.qrData());
        System.out.println("=================================");

        if (request.registrationNumber() == null ||
                request.registrationNumber().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Student registration number is required."
                    )
            );
        }

        if (request.qrData() == null ||
                request.qrData().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "QR data is empty."
                    )
            );
        }

        /*
         * TEMPORARY VALIDATION
         *
         * This is only for testing the complete
         * frontend -> backend flow.
         *
         * Later we will validate:
         *
         * 1. QR session
         * 2. QR token
         * 3. Expiry
         * 4. Student
         * 5. Duplicate attendance
         */

        if (!request.qrData().startsWith("ATTENDANCE:")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Invalid attendance QR code."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message",
                        "Attendance recorded for "
                                + request.registrationNumber(),
                        "scannedAt",
                        OffsetDateTime.now().toString()
                )
        );
    }

    public record AttendanceScanRequest(
            String registrationNumber,
            String qrData
    ) {
    }
}