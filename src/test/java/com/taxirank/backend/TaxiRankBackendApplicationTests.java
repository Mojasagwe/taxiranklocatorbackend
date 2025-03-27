package com.taxirank.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TaxiRankBackendApplicationTests {

	@Test
	@Disabled("Temporarily disabled until the test infrastructure is properly configured")
	void contextLoads() {
	}

}
