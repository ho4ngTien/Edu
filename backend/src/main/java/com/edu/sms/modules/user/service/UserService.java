package com.edu.sms.modules.user.service;

import com.edu.sms.common.exception.ResourceNotFoundException;
import com.edu.sms.modules.user.dto.UserRequest;
import com.edu.sms.modules.user.dto.UserResponse;
import com.edu.sms.modules.user.model.Profile;
import com.edu.sms.modules.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final ProfileRepository profileRepository;

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarDir;

    @Transactional(readOnly = true)
    public UserResponse getProfileByAuthUserId(UUID authUserId) {
        Profile profile = findProfileByAuthUserId(authUserId);
        return mapToResponse(profile);
    }

    @Transactional
    public UserResponse updateProfile(UUID authUserId, UserRequest request) {
        Profile profile = findProfileByAuthUserId(authUserId);

        if (StringUtils.hasText(request.getFullName())) {
            profile.setFullName(request.getFullName());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }

        return mapToResponse(profileRepository.save(profile));
    }

    @Transactional
    public UserResponse uploadAvatar(UUID authUserId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh đại diện không được để trống");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh JPEG, PNG, WEBP hoặc GIF");
        }

        Profile profile = findProfileByAuthUserId(authUserId);
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
        String filename = authUserId + "_" + UUID.randomUUID() + extension;

        try {
            Path directory = Paths.get(avatarDir);
            Files.createDirectories(directory);
            Path target = directory.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu ảnh đại diện: " + e.getMessage());
        }

        String avatarUrl = "/uploads/avatars/" + filename;
        profile.setAvatarUrl(avatarUrl);
        return mapToResponse(profileRepository.save(profile));
    }

    private Profile findProfileByAuthUserId(UUID authUserId) {
        return profileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ cá nhân"));
    }

    private UserResponse mapToResponse(Profile profile) {
        return UserResponse.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .phone(profile.getPhone())
                .build();
    }
}
