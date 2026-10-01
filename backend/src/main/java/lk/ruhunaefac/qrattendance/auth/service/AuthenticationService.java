package lk.ruhunaefac.qrattendance.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.auth.dto.LoginRequest;
import lk.ruhunaefac.qrattendance.auth.dto.RegisterRequest;
import lk.ruhunaefac.qrattendance.auth.entity.AuthSession;
import lk.ruhunaefac.qrattendance.auth.repository.AuthSessionRepository;
import lk.ruhunaefac.qrattendance.user.entity.Lecturer;
import lk.ruhunaefac.qrattendance.user.entity.Student;
import lk.ruhunaefac.qrattendance.user.entity.User;
import lk.ruhunaefac.qrattendance.user.repository.LecturerRepository;
import lk.ruhunaefac.qrattendance.user.repository.StudentRepository;
import lk.ruhunaefac.qrattendance.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AuthenticationManager authenticationManager;
    private final AuthSessionRepository sessions;
    private final UserRepository users;
    private final StudentRepository students;
    private final LecturerRepository lecturers;
    private final PasswordEncoder passwordEncoder;
    private final String studentRegistrationCode;
    private final String lecturerRegistrationCode;

    public AuthenticationService(AuthenticationManager authenticationManager, AuthSessionRepository sessions,
            UserRepository users, StudentRepository students, LecturerRepository lecturers, PasswordEncoder passwordEncoder,
            @Value("${app.security.student-registration-code:}") String studentRegistrationCode,
            @Value("${app.security.lecturer-registration-code:}") String lecturerRegistrationCode) {
        this.authenticationManager = authenticationManager; this.sessions = sessions; this.users = users;
        this.students = students; this.lecturers = lecturers; this.passwordEncoder = passwordEncoder;
        this.studentRegistrationCode = studentRegistrationCode; this.lecturerRegistrationCode = lecturerRegistrationCode;
    }

    @Transactional
    public IssuedSession login(LoginRequest request) {
        String identifier = request.username().strip();
        String username = users.findByUsernameIgnoreCase(identifier).isPresent() ? identifier
                : students.findByStudentIdIgnoreCase(identifier)
                        .map(student -> student.getUser().getUsername()).orElse(identifier);
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, request.password()));
        User user = users.findByUsernameIgnoreCase(authentication.getName()).orElseThrow();
        return issue(user);
    }

    @Transactional
    public IssuedSession register(User.Role role, RegisterRequest request) {
        String expectedCode = role == User.Role.STUDENT ? studentRegistrationCode : lecturerRegistrationCode;
        if (expectedCode.isBlank() || !MessageDigest.isEqual(expectedCode.getBytes(StandardCharsets.UTF_8), request.registrationCode().getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account registration is unavailable or the registration code is invalid");
        }
        if (!request.password().equals(request.confirmPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        if (role == User.Role.STUDENT && request.department() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department is required");
        if (role == User.Role.LECTURER && request.department() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department is only valid for student accounts");
        String username = request.username().strip();
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (role == User.Role.STUDENT && users.existsByUsernameIgnoreCase(username)) throw duplicate();
        if (role == User.Role.LECTURER && users.existsByUsernameIgnoreCase(username)) throw duplicate();
        if (users.existsByEmailIgnoreCase(email)) throw duplicate();
        User user = new User(); user.setUsername(username); user.setEmail(email); user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        users.saveAndFlush(user);
        if (role == User.Role.STUDENT) {
            if (students.existsByStudentIdIgnoreCase(request.institutionalId().strip())) throw duplicate();
            Student student = new Student(); student.setUser(user); student.setStudentId(request.institutionalId().strip());
            student.setFullName(request.fullName().strip());
            student.setDepartment(request.department());
            students.save(student);
            user.setStudent(student);
        } else {
            if (lecturers.existsByLecturerIdIgnoreCase(request.institutionalId().strip())) throw duplicate();
            Lecturer lecturer = new Lecturer(); lecturer.setUser(user); lecturer.setLecturerId(request.institutionalId().strip());
            lecturer.setFullName(request.fullName().strip()); lecturers.save(lecturer); user.setLecturer(lecturer);
        }
        return issue(user);
    }

    @Transactional(readOnly = true)
    public User currentUser(String rawToken) {
        return sessions.findByTokenHashAndExpiresAtAfter(hash(rawToken), Instant.now())
                .map(AuthSession::getUser).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @Transactional
    public void logout(String rawToken) { if (rawToken != null && !rawToken.isBlank()) sessions.deleteByTokenHash(hash(rawToken)); }

    private IssuedSession issue(User user) {
        sessions.deleteByExpiresAtBefore(Instant.now());
        byte[] raw = new byte[32]; RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        AuthSession session = new AuthSession(UUID.randomUUID(), user, hash(token),
                Instant.now(), Instant.now().plus(8, ChronoUnit.HOURS));
        sessions.save(session);
        return new IssuedSession(token, user);
    }

    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    private ResponseStatusException duplicate() { return new ResponseStatusException(HttpStatus.CONFLICT, "Account details already exist"); }
    public record IssuedSession(String token, User user) {
        @Override public String toString() { return "IssuedSession[token=[REDACTED], user=" + user.getUsername() + "]"; }
    }
}
