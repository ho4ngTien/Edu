package com.edu.sms.modules.admin.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.common.dto.PageResponse;
import com.edu.sms.modules.admin.dto.*;
import com.edu.sms.modules.admin.service.AdminService;
import com.edu.sms.modules.admin.service.AdminUserManagementService;
import com.edu.sms.modules.auth.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminUserManagementService adminUserManagementService;

    // ─── System config ────────────────────────────────────────────────────

    @GetMapping("/configs/{configId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponse>> getConfig(@PathVariable String configId) {
        AdminResponse response = adminService.getConfig(configId);
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình thành công", response));
    }

    @PutMapping("/configs/{configId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponse>> updateConfig(
            @PathVariable String configId,
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody AdminRequest request) {
        AdminResponse response = adminService.updateConfig(configId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình thành công", response));
    }

    // ─── Students ─────────────────────────────────────────────────────────

    @PostMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StudentAdminResponse>> createStudent(
            @Valid @RequestBody StudentAdminRequest request) {
        StudentAdminResponse response = adminUserManagementService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo học sinh thành công", response));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<StudentAdminResponse>>> listStudents(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "studentCode", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<StudentAdminResponse> response = adminUserManagementService.listStudents(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học sinh thành công", response));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StudentAdminResponse>> getStudent(@PathVariable UUID id) {
        StudentAdminResponse response = adminUserManagementService.getStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin học sinh thành công", response));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StudentAdminResponse>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody StudentAdminUpdateRequest request) {
        StudentAdminResponse response = adminUserManagementService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học sinh thành công", response));
    }

    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        adminUserManagementService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa học sinh thành công", null));
    }

    // ─── Teachers ─────────────────────────────────────────────────────────

    @PostMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TeacherAdminResponse>> createTeacher(
            @Valid @RequestBody TeacherAdminRequest request) {
        TeacherAdminResponse response = adminUserManagementService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo giáo viên thành công", response));
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TeacherAdminResponse>>> listTeachers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "teacherCode", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<TeacherAdminResponse> response = adminUserManagementService.listTeachers(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giáo viên thành công", response));
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TeacherAdminResponse>> getTeacher(@PathVariable UUID id) {
        TeacherAdminResponse response = adminUserManagementService.getTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin giáo viên thành công", response));
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TeacherAdminResponse>> updateTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody TeacherAdminUpdateRequest request) {
        TeacherAdminResponse response = adminUserManagementService.updateTeacher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giáo viên thành công", response));
    }

    @DeleteMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID id) {
        adminUserManagementService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa giáo viên thành công", null));
    }
}
