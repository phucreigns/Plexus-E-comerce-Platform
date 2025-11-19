package com.example.Ecom;

import com.phuc.notification.NotificationServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = NotificationServiceApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
class EcomApplicationTests {

	@Test
	void contextLoads() {
	}

}
