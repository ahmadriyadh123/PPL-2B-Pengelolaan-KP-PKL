package com.jtk.ps.api.java.unit;

import com.jtk.ps.api.helper.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class UT010_GradeKafkaListenerTest {

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Test
    void testConsumeEvaluation_WithInvalidJson_ShouldNotThrowException() {
        // BUG-010: Memastikan listener Kafka mampu menangani pesan rusak tanpa menyebabkan aplikasi berhenti
        String invalidJson = "invalid string { { {";

        // Asserting that the method catches JsonProcessingException and doesn't throw to the caller
        assertDoesNotThrow(() -> kafkaConsumer.consumeEvaluation(invalidJson));
    }
}
