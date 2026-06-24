package com.jtk.ps.api.java.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"spring.embedded.kafka.brokers=localhost:9092"})
@ActiveProfiles("test")
public class UT017_DatabaseIsolationTest {

    @Autowired
    private Environment environment;

    @Test
    void testDatabaseIsolation_ShouldUseH2InMemoryDatabase() {
        // BUG-017 Test Database Isolation
        String dbUrl = environment.getProperty("spring.datasource.url");

        // Memastikan konfigurasi DB menunjuk ke h2 in memory dan bukan MySQL production
        assertTrue(dbUrl != null && dbUrl.contains("jdbc:h2:mem"));
    }
}
