package com.edu.sms.modules.academic.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.academic.dto.AcademicRequest;
import com.edu.sms.modules.academic.dto.AcademicResponse;
import com.edu.sms.modules.academic.service.AcademicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<AcademicResponse>>> getAllCourses() {
        List<AcademicResponse> response = academicService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục môn học thành công", response));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<AcademicResponse>> createCourse(
            @Valid @RequestBody AcademicRequest request) {
        AcademicResponse response = academicService.createCourse(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo môn học thành công", response));
    }
}
