package lk.ruhunaefac.qrattendance;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

@SpringBootTest(properties = {
        "attendance.qr-token-secret=test-only-qr-signing-secret",
        "spring.datasource.url=jdbc:h2:mem:qrtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.security.cookie-secure=false",
        "app.security.lecturer-registration-code=lecturer-test-registration-code"
})
@AutoConfigureMockMvc
class QrAttendanceBackendApplicationTests {
    @Autowired private MockMvc mvc;

    @Test void sampleLecturerCanRegisterLoginAndReadProfile() throws Exception {
        String username = "sample-lecturer-validation";
        String password = "SampleLecturerValidation2026!";
        mvc.perform(post("/api/auth/register/lecturer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Sample Lecturer","username":"%s","email":"sample-lecturer-validation@example.edu",
                                 "password":"%s","confirmPassword":"%s","institutionalId":"LECT/QA/1001",
                                 "registrationCode":"lecturer-test-registration-code"}
                                """.formatted(username, password, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("LECTURER"))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.institutionalId").value("LECT/QA/1001"));

        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("LECTURER"));
    }

    @Test void attendanceRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/attendance/lecturer-dashboard")).andExpect(status().isUnauthorized());
    }

    @Test void studentCannotStartLecturerSession() throws Exception {
        mvc.perform(post("/api/attendance/sessions").with(user("student").roles("STUDENT")).with(csrf())
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test void lecturerCannotCheckInAsStudent() throws Exception {
        mvc.perform(post("/api/attendance/check-in").with(user("lecturer").roles("LECTURER")).with(csrf())
                .contentType("application/json").content("{\"qrToken\":\"token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void studentCannotReadLecturerDashboard() throws Exception {
        mvc.perform(get("/api/attendance/lecturer-dashboard").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test void currentUserRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void publicRegistrationRequiresConfiguredInvitationCode() throws Exception {
        mvc.perform(post("/api/auth/register/lecturer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"fullName":"Example Lecturer","username":"example.lecturer","email":"lecturer@example.edu",
                         "password":"a-long-test-password","confirmPassword":"a-long-test-password",
                         "institutionalId":"L-100","registrationCode":"incorrect-code"}
                        """))
                .andExpect(status().isForbidden());
    }
}
