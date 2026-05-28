package com.edu.sms.modules.attendance.service;

import com.edu.sms.modules.attendance.dto.AttendanceRequest;
import com.edu.sms.modules.attendance.dto.AttendanceResponse;
import com.edu.sms.modules.attendance.model.Attendance;
import com.edu.sms.modules.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Transactional
    public AttendanceResponse recordAttendance(UUID classId, AttendanceRequest request, UUID recordedBy) {
        Attendance attendance = Attendance.builder()
                .classId(classId)
                .studentId(request.getStudentId())
                .date(request.getDate())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .recordedBy(recordedBy)
                .build();
        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByStudent(UUID studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .classId(attendance.getClassId())
                .studentId(attendance.getStudentId())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .build();
    }
}
