package com.edu.sms.modules.academic.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.academic.dto.AcademicClassRequest;
import com.edu.sms.modules.academic.dto.AcademicClassResponse;
import com.edu.sms.modules.academic.dto.AddStudentsRequest;
import com.edu.sms.modules.academic.dto.ClassStudentResponse;
import com.edu.sms.modules.academic.dto.CourseRequest;
import com.edu.sms.modules.academic.dto.CourseResponse;
import com.edu.sms.modules.academic.dto.ScheduleRequest;
import com.edu.sms.modules.academic.dto.ScheduleResponse;
import com.edu.sms.modules.academic.dto.SemesterRequest;
import com.edu.sms.modules.academic.dto.SemesterResponse;
import com.edu.sms.modules.academic.dto.TeacherAssignmentRequest;
import com.edu.sms.modules.academic.dto.AcademicResponse;
import com.edu.sms.modules.academic.service.AcademicService;
import com.edu.sms.modules.auth.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<AcademicResponse>>> getAllCourses(
            @RequestParam(required = false) UUID semesterId) {
        List<AcademicResponse> response = academicService.getAllCourses(semesterId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách môn học thành công", response));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable UUID courseId) {
        CourseResponse response = academicService.getCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin môn học thành công", response));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = academicService.createCourse(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo môn học thành công", response));
    }

    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = academicService.updateCourse(courseId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật môn học thành công", response));
    }

    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId) {
        academicService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Xoá môn học thành công", null));
    }

    @GetMapping("/semesters")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getAllSemesters() {
        List<SemesterResponse> response = academicService.getAllSemesters();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học kỳ thành công", response));
    }

    @GetMapping("/semesters/{semesterId}")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemesterById(@PathVariable UUID semesterId) {
        SemesterResponse response = academicService.getSemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin học kỳ thành công", response));
    }

    @PostMapping("/semesters")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(
            @Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = academicService.createSemester(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo học kỳ thành công", response));
    }

    @PutMapping("/semesters/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemester(
            @PathVariable UUID semesterId,
            @Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = academicService.updateSemester(semesterId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật học kỳ thành công", response));
    }

    @DeleteMapping("/semesters/{semesterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(@PathVariable UUID semesterId) {
        academicService.deleteSemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success("Xoá học kỳ thành công", null));
    }

    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<AcademicClassResponse>>> getClasses(
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) UUID teacherId) {
        List<AcademicClassResponse> response = academicService.getClasses(semesterId, teacherId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp học thành công", response));
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> getClassById(@PathVariable UUID classId) {
        AcademicClassResponse response = academicService.getClassById(classId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lớp học thành công", response));
    }

    @PostMapping("/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> createClass(
            @Valid @RequestBody AcademicClassRequest request) {
        AcademicClassResponse response = academicService.createClass(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo lớp học thành công", response));
    }

    @PutMapping("/classes/{classId}/teacher")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> assignTeacher(
            @PathVariable UUID classId,
            @Valid @RequestBody TeacherAssignmentRequest request) {
        AcademicClassResponse response = academicService.assignTeacher(classId, request);
        return ResponseEntity.ok(ApiResponse.success("Gán giáo viên thành công", response));
    }

    @GetMapping("/classes/{classId}/students")
    public ResponseEntity<ApiResponse<List<ClassStudentResponse>>> getClassStudents(
            @PathVariable UUID classId) {
        List<ClassStudentResponse> response = academicService.getStudents(classId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học sinh trong lớp thành công", response));
    }

    @PostMapping("/classes/{classId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ClassStudentResponse>>> addStudents(
            @PathVariable UUID classId,
            @Valid @RequestBody AddStudentsRequest request) {
        List<ClassStudentResponse> response = academicService.addStudents(classId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm học sinh vào lớp thành công", response));
    }

    @GetMapping("/classes/{classId}/schedules")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getClassSchedules(
            @PathVariable UUID classId) {
        List<ScheduleResponse> response = academicService.getSchedulesByClass(classId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch học theo lớp thành công", response));
    }

    @PostMapping("/classes/{classId}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createClassSchedule(
            @PathVariable UUID classId,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = academicService.createSchedule(classId, request);
        return ResponseEntity.ok(ApiResponse.success("Tạo thời khóa biểu thành công", response));
    }

    @GetMapping("/schedules/my-schedule")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getMySchedule(
            @AuthenticationPrincipal AuthUser currentUser) {
        List<ScheduleResponse> response = academicService.getMySchedule(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thời khóa biểu cá nhân thành công", response));
    }
}
