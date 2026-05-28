package com.edu.sms.modules.user.controller;

import com.edu.sms.common.dto.ApiResponse;
import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.user.dto.UserRequest;
import com.edu.sms.modules.user.dto.UserResponse;
import com.edu.sms.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal AuthUser currentUser) {
        UserResponse response = userService.getProfileByAuthUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân thành công", response));
    }

    @PutMapping("/profile/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", response));
    }
}
