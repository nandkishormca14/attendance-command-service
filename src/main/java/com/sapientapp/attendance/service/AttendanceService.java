package com.sapientapp.attendance.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sapientapp.attendance.dto.AttendanceStatusDTO;
import com.sapientapp.attendance.kafka.KafkaProducer;
import com.sapientapp.attendance.model.Attendance;
import com.sapientapp.attendance.repository.AttendanceRepository;

@Service
public class AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private KafkaProducer kafkaProducer;

    public void swipeIn(Long employeeId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Attendance attendance = new Attendance();
            attendance.setEmployeeId(employeeId);
            attendance.setDate(LocalDate.now());
            attendance.setSwipeIn(now);
            attendanceRepository.save(attendance);
        } catch (Exception e) {
            logger.error("Error during swipe in: {}", e.getMessage());
            throw new RuntimeException("Error during swipe in", e);
        }
    }

    public void swipeOut(Long employeeId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Optional<Attendance> latestSwipeIn = attendanceRepository.findTopByEmployeeIdAndDateOrderBySwipeInDesc(employeeId, LocalDate.now());

            if (latestSwipeIn.isPresent()) {
                Attendance attendance = latestSwipeIn.get();
                attendance.setSwipeOut(now);
                attendanceRepository.save(attendance);
            } else {
                logger.error("No matching swipe-in record found for employeeId: {}", employeeId);
                throw new RuntimeException("No matching swipe-in record found.");
            }
        } catch (Exception e) {
            logger.error("Error during swipe out: {}", e.getMessage());
            throw new RuntimeException("Error during swipe out", e);
        }
    }

    public String attendanceStatus(Long employeeId, LocalDate date) {
        try {
            List<Attendance> attendanceList = attendanceRepository.findByEmployeeIdAndDate(employeeId, date);

            AttendanceStatusDTO attendanceStatusDTO = new AttendanceStatusDTO();

            if (attendanceList.isEmpty()) {
                attendanceStatusDTO.setEmployeeId(employeeId);
                attendanceStatusDTO.setDate(date);
                attendanceStatusDTO.setStatus("Absent");
                kafkaProducer.publish("attendance-event-topic", attendanceStatusDTO);
                return "Absent";
            }

            long totalHours = calculateTotalHours(attendanceList);

            if (totalHours < 4) {
                attendanceStatusDTO.setEmployeeId(employeeId);
                attendanceStatusDTO.setDate(date);
                attendanceStatusDTO.setStatus("Absent");
                kafkaProducer.publish("attendance-event-topic", attendanceStatusDTO);
                return "Absent";
            } else if (totalHours < 8) {
                attendanceStatusDTO.setEmployeeId(employeeId);
                attendanceStatusDTO.setDate(date);
                attendanceStatusDTO.setStatus("Half day");
                kafkaProducer.publish("attendance-event-topic", attendanceStatusDTO);
                return "Half day";
            } else {
                attendanceStatusDTO.setEmployeeId(employeeId);
                attendanceStatusDTO.setDate(date);
                attendanceStatusDTO.setStatus("Present");
                kafkaProducer.publish("attendance-event-topic", attendanceStatusDTO);
                return "Present";
            }
        } catch (Exception e) {
            logger.error("Error during attendance status calculation: {}", e.getMessage());
            throw new RuntimeException("Error during attendance status", e);
        }
    }

    public long calculateTotalHours(List<Attendance> attendanceList) {
        long totalHours = 0;

        for (Attendance attendance : attendanceList) {
            LocalDateTime swipeIn = attendance.getSwipeIn();
            LocalDateTime swipeOut = attendance.getSwipeOut();

            if (swipeIn != null && swipeOut != null) {
                // Calculate the duration between swipe-in and swipe-out
                Duration duration = Duration.between(swipeIn, swipeOut);
                totalHours += duration.toMillis();
            }
        }

        return totalHours / (1000 * 60 * 60);
    }
}
