package com.sapientapp.attendance.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sapientapp.attendance.dto.AttendanceStatusDTO;

@Component
public class KafkaProducer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, AttendanceStatusDTO message) {
        try {
            kafkaTemplate.send(topic, message);
            logger.info("Message published successfully to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Error publishing message to Kafka topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Error publishing message to Kafka", e);
        }
    }
}
