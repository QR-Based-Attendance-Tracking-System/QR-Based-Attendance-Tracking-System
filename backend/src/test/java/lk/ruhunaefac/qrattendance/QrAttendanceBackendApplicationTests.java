package lk.ruhunaefac.qrattendance;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        "spring.jpa.hibernate.ddl-auto=none",
        "app.security.cookie-secure=false"
})
@AutoConfigureMockMvc
class QrAttendanceBackendApplicationTests {
    @Autowired private MockMvc mvc;

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
