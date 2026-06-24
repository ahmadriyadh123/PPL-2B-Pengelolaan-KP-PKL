package com.jtk.ps.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.embedded.kafka.brokers=localhost:9092"})
class GradeServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
