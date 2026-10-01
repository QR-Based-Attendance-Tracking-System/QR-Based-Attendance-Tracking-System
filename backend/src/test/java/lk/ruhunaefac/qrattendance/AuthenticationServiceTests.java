package lk.ruhunaefac.qrattendance;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import lk.ruhunaefac.qrattendance.auth.dto.LoginRequest;
import lk.ruhunaefac.qrattendance.auth.dto.RegisterRequest;
import lk.ruhunaefac.qrattendance.auth.entity.AuthSession;
import lk.ruhunaefac.qrattendance.auth.repository.AuthSessionRepository;
import lk.ruhunaefac.qrattendance.auth.service.AuthenticationService;
import lk.ruhunaefac.qrattendance.user.entity.User;
import lk.ruhunaefac.qrattendance.user.entity.Student;
import lk.ruhunaefac.qrattendance.user.repository.LecturerRepository;
import lk.ruhunaefac.qrattendance.user.repository.StudentRepository;
import lk.ruhunaefac.qrattendance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthenticationServiceTests {
    @Test void studentRegistrationPersistsSelectedDepartment() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthSessionRepository sessions = mock(AuthSessionRepository.class);
        UserRepository users = mock(UserRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        LecturerRepository lecturers = mock(LecturerRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("a-long-test-password")).thenReturn("hashed-password");
        var service = new AuthenticationService(manager, sessions, users, students, lecturers, encoder,
                "student-registration-code-0123456789", "lecturer-registration-code-0123456789");
        var request = new RegisterRequest("Test Student", "test-student", "test@example.edu",
                "a-long-test-password", "a-long-test-password", "S-100", "student-registration-code-0123456789",
                Student.Department.MECH);

        service.register(User.Role.STUDENT, request);

        var saved = org.mockito.ArgumentCaptor.forClass(Student.class);
        verify(students).save(saved.capture());
        org.junit.jupiter.api.Assertions.assertEquals(Student.Department.MECH, saved.getValue().getDepartment());
    }

    @Test void loginIssuesOpaqueTokenAndStoresOnlyItsDigest() throws Exception {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthSessionRepository sessions = mock(AuthSessionRepository.class);
        UserRepository users = mock(UserRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        LecturerRepository lecturers = mock(LecturerRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        User user = new User(); user.setUsername("student-1"); user.setRole(User.Role.STUDENT);
        when(manager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                "student-1", null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));
        when(users.findByUsernameIgnoreCase("student-1")).thenReturn(Optional.of(user));
        var service = new AuthenticationService(manager, sessions, users, students, lecturers, encoder, "student-code", "lecturer-code");

        var issued = service.login(new LoginRequest("student-1", "long-test-password"));
        var saved = org.mockito.ArgumentCaptor.forClass(AuthSession.class);
        verify(sessions).save(saved.capture());
        String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(issued.token().getBytes(StandardCharsets.UTF_8)));
        assertNotEquals(issued.token(), saved.getValue().getTokenHash());
        assertTrue(expectedHash.equals(saved.getValue().getTokenHash()));
        assertTrue(issued.toString().contains("[REDACTED]"));
    }

    @Test void studentCanLoginUsingStudentId() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthSessionRepository sessions = mock(AuthSessionRepository.class);
        UserRepository users = mock(UserRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        LecturerRepository lecturers = mock(LecturerRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        User user = new User(); user.setUsername("student-login-name"); user.setRole(User.Role.STUDENT);
        Student student = new Student(); student.setUser(user); student.setStudentId("QA/2026/1001");
        when(users.findByUsernameIgnoreCase("QA/2026/1001")).thenReturn(Optional.empty());
        when(students.findByStudentIdIgnoreCase("QA/2026/1001")).thenReturn(Optional.of(student));
        when(manager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                "student-login-name", null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));
        when(users.findByUsernameIgnoreCase("student-login-name")).thenReturn(Optional.of(user));
        var service = new AuthenticationService(manager, sessions, users, students, lecturers, encoder, "student-code", "lecturer-code");

        var issued = service.login(new LoginRequest("QA/2026/1001", "long-test-password"));

        assertTrue(issued.user() == user);
        var token = org.mockito.ArgumentCaptor.forClass(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class);
        verify(manager).authenticate(token.capture());
        org.junit.jupiter.api.Assertions.assertEquals("student-login-name", token.getValue().getName());
    }
}
