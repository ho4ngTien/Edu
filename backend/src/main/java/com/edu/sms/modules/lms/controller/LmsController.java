package com.edu.sms.modules.lms.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.lms.dto.LmsRequest;
import com.edu.sms.modules.lms.dto.LmsResponse;
import com.edu.sms.modules.lms.service.LmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lms")
@RequiredArgsConstructor
public class LmsController {

    private final LmsService lmsService;

    @PostMapping("/classes/{classId}/assignments")
    public ResponseEntity<ApiResponse<LmsResponse>> createAssignment(
            @PathVariable UUID classId,
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody LmsRequest request) {
        LmsResponse response = lmsService.createAssignment(classId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Tạo bài tập thành công", response));
    }

    @GetMapping("/classes/{classId}/assignments")
    public ResponseEntity<ApiResponse<List<LmsResponse>>> getAssignments(
            @PathVariable UUID classId) {
        List<LmsResponse> response = lmsService.getAssignmentsByClass(classId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài tập thành công", response));
    }
}
