package com.edu.sms.modules.attendance.repository;

import com.edu.sms.modules.attendance.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByClassIdAndDate(UUID classId, LocalDate date);
    List<Attendance> findByStudentId(UUID studentId);
}
