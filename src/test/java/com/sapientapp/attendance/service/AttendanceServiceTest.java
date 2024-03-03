package com.sapientapp.attendance.service;

import com.sapientapp.attendance.dto.AttendanceStatusDTO;
import com.sapientapp.attendance.kafka.KafkaProducer;
import com.sapientapp.attendance.model.Attendance;
import com.sapientapp.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void testSwipeInSuccessful() {
        Long employeeId = 1L;
        LocalDateTime now = LocalDateTime.now();

        attendanceService.swipeIn(employeeId);

        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testSwipeInExceptionThrown() {
        Long employeeId = 1L;
        doThrow(new RuntimeException("Test Exception")).when(attendanceRepository).save(any(Attendance.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.swipeIn(employeeId);
        });

        assertEquals("Error during swipe in", exception.getMessage());
    }

    @Test
    void testSwipeOutSuccessful() {
        Long employeeId = 1L;
        LocalDateTime now = LocalDateTime.now();
        List<Attendance> attendanceList = new ArrayList<>();
        attendanceList.add(new Attendance());

        when(attendanceRepository.findTopByEmployeeIdAndDateOrderBySwipeInDesc(anyLong(), any(LocalDate.class))).thenReturn(Optional.of(new Attendance()));

        attendanceService.swipeOut(employeeId);

        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testSwipeOutNoMatchingRecord() {
        Long employeeId = 1L;
        when(attendanceRepository.findTopByEmployeeIdAndDateOrderBySwipeInDesc(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.swipeOut(employeeId);
        });

        assertEquals("Error during swipe out", exception.getMessage());
    }

    @Test
    void testSwipeOutExceptionThrown() {
        Long employeeId = 1L;
        when(attendanceRepository.findTopByEmployeeIdAndDateOrderBySwipeInDesc(anyLong(), any(LocalDate.class))).thenReturn(Optional.of(new Attendance()));
        doThrow(new RuntimeException("Test Exception")).when(attendanceRepository).save(any(Attendance.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.swipeOut(employeeId);
        });

        assertEquals("Error during swipe out", exception.getMessage());
    }

    @Test
    void testAttendanceStatusAbsent() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.now();

        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(new ArrayList<>());

        String status = attendanceService.attendanceStatus(employeeId, date);

        assertEquals("Absent", status);
        verify(kafkaProducer).publish(anyString(), any(AttendanceStatusDTO.class));
    }

    @Test
    void testAttendanceStatusHalfDay() {
        Long employeeId = 1L;
        Long id = 1L;
        LocalDate date = LocalDate.now();
        List<Attendance> attendanceList = new ArrayList<>();
        attendanceList.add(new Attendance(id,employeeId, date, LocalDateTime.now().minusHours(6), LocalDateTime.now()));

        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(attendanceList);

        String status = attendanceService.attendanceStatus(employeeId, date);

        assertEquals("Half day", status);
        verify(kafkaProducer).publish(anyString(), any(AttendanceStatusDTO.class));
    }

    @Test
    void testAttendanceStatusPresent() {
        Long employeeId = 1L;
        Long id = 1l;
        LocalDate date = LocalDate.now();
        List<Attendance> attendanceList = new ArrayList<>();
        attendanceList.add(new Attendance(id,employeeId, date, LocalDateTime.now().minusHours(6), LocalDateTime.now()));

        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(attendanceList);

        String status = attendanceService.attendanceStatus(employeeId, date);

        assertEquals("Half day", status);
        verify(kafkaProducer).publish(anyString(), any(AttendanceStatusDTO.class));
    }

    @Test
    void testAttendanceStatusExceptionThrown() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.now();

        when(attendanceRepository.findByEmployeeIdAndDate(anyLong(), any(LocalDate.class))).thenThrow(new RuntimeException("Test Exception"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.attendanceStatus(employeeId, date);
        });

        assertEquals("Error during attendance status", exception.getMessage());
    }

    @Test
    void testCalculateTotalHours() {
    	Long employeeId = 1L;
        Long id = 1l;
        LocalDate date = LocalDate.now();
        List<Attendance> attendanceList = new ArrayList<>();
        attendanceList.add(new Attendance(id,employeeId, date, LocalDateTime.now().minusHours(6), LocalDateTime.now()));

        long totalHours = attendanceService.calculateTotalHours(attendanceList);

        assertEquals(6, totalHours);
    }
}
