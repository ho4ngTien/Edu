package com.edu.sms.modules.admin.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.admin.dto.AdminRequest;
import com.edu.sms.modules.admin.dto.AdminResponse;
import com.edu.sms.modules.admin.service.AdminService;
import com.edu.sms.modules.auth.model.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/configs/{configId}")
    public ResponseEntity<ApiResponse<AdminResponse>> getConfig(
            @PathVariable String configId) {
        AdminResponse response = adminService.getConfig(configId);
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình thành công", response));
    }

    @PutMapping("/configs/{configId}")
    public ResponseEntity<ApiResponse<AdminResponse>> updateConfig(
            @PathVariable String configId,
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody AdminRequest request) {
        AdminResponse response = adminService.updateConfig(configId, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình thành công", response));
    }
}
