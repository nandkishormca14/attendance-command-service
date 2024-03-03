package com.sapientapp.attendance.kafka;

import com.sapientapp.attendance.dto.AttendanceStatusDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducer kafkaProducer;

    @Test
    void testPublishSuccessful() {
        String topic = "test-topic";
        AttendanceStatusDTO message = new AttendanceStatusDTO();

        kafkaProducer.publish(topic, message);

        verify(kafkaTemplate).send(eq(topic), eq(message));
    }

    @Test
    void testPublishExceptionThrown() {
        String topic = "test-topic";
        AttendanceStatusDTO message = new AttendanceStatusDTO();

        doThrow(new RuntimeException("Test Exception")).when(kafkaTemplate).send(anyString(), any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kafkaProducer.publish(topic, message);
        });

        assertEquals("Error publishing message to Kafka", exception.getMessage());
        verify(kafkaTemplate).send(eq(topic), eq(message));
    }
}

