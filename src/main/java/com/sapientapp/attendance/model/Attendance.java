package com.sapientapp.attendance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "attendance_command")
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id")
    private Long employeeId;
    
    private LocalDate date;
    
    @Column(name = "swipe_in")
    private LocalDateTime swipeIn;
    
    @Column(name = "swipe_out")
    private LocalDateTime swipeOut;
	
}
