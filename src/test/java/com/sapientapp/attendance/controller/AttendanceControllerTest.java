package com.sapientapp.attendance.controller;

import com.sapientapp.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    @Test
    void swipeInSuccessful() {
        ResponseEntity<String> responseEntity = attendanceController.swipeIn(1L);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Swiped in successfully", responseEntity.getBody());
        verify(attendanceService).swipeIn(1L);
    }

    @Test
    void swipeInExceptionThrown() {
        doThrow(new RuntimeException("Test Exception")).when(attendanceService).swipeIn(anyLong());

        ResponseEntity<String> responseEntity = attendanceController.swipeIn(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Test Exception", responseEntity.getBody());
        verify(attendanceService).swipeIn(1L);
    }

    @Test
    void swipeOutSuccessful() {
        ResponseEntity<String> responseEntity = attendanceController.swipeOut(1L);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Swiped out successfully", responseEntity.getBody());
        verify(attendanceService).swipeOut(1L);
    }

    @Test
    void swipeOutExceptionThrown() {
        doThrow(new RuntimeException("Test Exception")).when(attendanceService).swipeOut(anyLong());

        ResponseEntity<String> responseEntity = attendanceController.swipeOut(1L);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Test Exception", responseEntity.getBody());
        verify(attendanceService).swipeOut(1L);
    }

    @Test
    void attendanceStatusSuccessful() {
        String expectedStatus = "Present";
        LocalDate date = LocalDate.now();
        doReturn(expectedStatus).when(attendanceService).attendanceStatus(anyLong(), ArgumentMatchers.any(LocalDate.class));


        ResponseEntity<String> responseEntity = attendanceController.attendanceStatus(1L, date);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Attendance Status: " + expectedStatus, responseEntity.getBody());
        verify(attendanceService).attendanceStatus(1L, date);
    }

    @Test
    void attendanceStatusExceptionThrown() {
        LocalDate date = LocalDate.now();
      
        doThrow(new RuntimeException("Test Exception")).when(attendanceService).attendanceStatus(anyLong(), ArgumentMatchers.any(LocalDate.class));

        ResponseEntity<String> responseEntity = attendanceController.attendanceStatus(1L, date);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Test Exception", responseEntity.getBody());
        verify(attendanceService).attendanceStatus(1L, date);
    }
}

