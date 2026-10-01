package lk.ruhunaefac.qrattendance.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lk.ruhunaefac.qrattendance.auth.dto.CurrentUserResponse;
import lk.ruhunaefac.qrattendance.auth.dto.LoginRequest;
import lk.ruhunaefac.qrattendance.auth.dto.RegisterRequest;
import lk.ruhunaefac.qrattendance.auth.service.AuthenticationService;
import lk.ruhunaefac.qrattendance.security.SessionAuthenticationFilter;
import lk.ruhunaefac.qrattendance.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService service;
    private final boolean secureCookie;
    public AuthenticationController(AuthenticationService service, @Value("${app.security.cookie-secure:true}") boolean secureCookie) {
        this.service = service; this.secureCookie = secureCookie;
    }
    @GetMapping("/csrf") public ResponseEntity<Void> csrf(CsrfToken token) {
        return ResponseEntity.noContent().header("X-CSRF-TOKEN", token.getToken()).build();
    }
    @PostMapping("/login") public CurrentUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var session = service.login(request); setSessionCookie(response, session.token());
        return CurrentUserResponse.from(session.user());
    }
    @PostMapping("/register/student") public ResponseEntity<CurrentUserResponse> registerStudent(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        var session = service.register(User.Role.STUDENT, request); setSessionCookie(response, session.token());
        return ResponseEntity.status(201).body(CurrentUserResponse.from(session.user()));
    }
    @PostMapping("/register/lecturer") public ResponseEntity<CurrentUserResponse> registerLecturer(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        var session = service.register(User.Role.LECTURER, request); setSessionCookie(response, session.token());
        return ResponseEntity.status(201).body(CurrentUserResponse.from(session.user()));
    }
    @GetMapping("/me") public CurrentUserResponse me(@AuthenticationPrincipal UserDetails principal, HttpServletRequest request) {
        return CurrentUserResponse.from(service.currentUser(SessionAuthenticationFilter.cookie(request)));
    }
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        service.logout(SessionAuthenticationFilter.cookie(request));
        ResponseCookie cookie = ResponseCookie.from(SessionAuthenticationFilter.COOKIE_NAME, "").httpOnly(true).secure(secureCookie)
                .sameSite("Lax").path("/").maxAge(Duration.ZERO).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString()); return ResponseEntity.noContent().build();
    }
    private void setSessionCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(SessionAuthenticationFilter.COOKIE_NAME, token).httpOnly(true)
                .secure(secureCookie).sameSite("Lax").path("/").maxAge(Duration.ofHours(8)).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
