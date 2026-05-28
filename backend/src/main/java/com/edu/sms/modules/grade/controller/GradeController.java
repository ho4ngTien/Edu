package com.edu.sms.modules.grade.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.grade.dto.GradeRequest;
import com.edu.sms.modules.grade.dto.GradeResponse;
import com.edu.sms.modules.grade.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grade")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<GradeResponse>> submitGrade(
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody GradeRequest request) {
        GradeResponse response = gradeService.submitGrade(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Nhập điểm thành công", response));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getStudentGrades(
            @PathVariable UUID studentId) {
        List<GradeResponse> response = gradeService.getGradesForStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy bảng điểm thành công", response));
    }
}
