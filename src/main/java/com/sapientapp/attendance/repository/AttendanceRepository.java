package com.sapientapp.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sapientapp.attendance.model.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    Optional<Attendance> findTopByEmployeeIdAndDateOrderBySwipeInDesc(Long employeeId, LocalDate date);
}
