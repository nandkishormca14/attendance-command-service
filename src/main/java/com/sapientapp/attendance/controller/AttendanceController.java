package com.sapientapp.attendance.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sapientapp.attendance.service.AttendanceService;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/swipe-in/{employeeId}")
    public ResponseEntity<String> swipeIn(@PathVariable Long employeeId) {
        try {
            attendanceService.swipeIn(employeeId);
            return ResponseEntity.ok("Swiped in successfully");
        } catch (Exception e) {
            logger.error("Error during swipe in: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/swipe-out/{employeeId}")
    public ResponseEntity<String> swipeOut(@PathVariable Long employeeId) {
        try {
            attendanceService.swipeOut(employeeId);
            return ResponseEntity.ok("Swiped out successfully");
        } catch (Exception e) {
            logger.error("Error during swipe out: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/publish-attendance-status/{employeeId}/{date}")
    public ResponseEntity<String> attendanceStatus(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            String status = attendanceService.attendanceStatus(employeeId, date);
            return ResponseEntity.ok("Attendance Status: " + status);
        } catch (Exception e) {
            logger.error("Error getting attendance status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
