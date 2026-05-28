package com.edu.sms.modules.attendance.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.attendance.dto.AttendanceRequest;
import com.edu.sms.modules.attendance.dto.AttendanceResponse;
import com.edu.sms.modules.attendance.service.AttendanceService;
import com.edu.sms.modules.auth.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/classes/{classId}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordAttendance(
            @PathVariable UUID classId,
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.recordAttendance(classId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Ghi nhận điểm danh thành công", response));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getStudentAttendance(
            @PathVariable UUID studentId) {
        List<AttendanceResponse> response = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin điểm danh thành công", response));
    }
}
