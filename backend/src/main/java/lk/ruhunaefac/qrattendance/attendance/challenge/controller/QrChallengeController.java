package lk.ruhunaefac.qrattendance.attendance.challenge.controller;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.attendance.challenge.dto.QrChallengeResponse;
import lk.ruhunaefac.qrattendance.attendance.challenge.service.QrChallengeService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/sessions")
public class QrChallengeController {
    private final QrChallengeService qrChallengeService;

    public QrChallengeController(QrChallengeService qrChallengeService) {
        this.qrChallengeService = qrChallengeService;
    }

    @GetMapping("/{sessionId}/qr-challenge")
    public ResponseEntity<QrChallengeResponse> getQrChallenge(@PathVariable UUID sessionId,
                                                              @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(qrChallengeService.createChallenge(sessionId, principal.getUsername()));
    }
}
