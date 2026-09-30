package lk.ruhunaefac.qrattendance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "attendance.qr-token-secret=test-only-qr-signing-secret")
class QrAttendanceBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
