package com.edu.sms.modules.user.service;

import com.edu.sms.modules.user.dto.UserRequest;
import com.edu.sms.modules.user.dto.UserResponse;
import com.edu.sms.modules.user.model.Profile;
import com.edu.sms.modules.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfileByAuthUserId(UUID authUserId) {
        Profile profile = profileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return mapToResponse(profile);
    }

    @Transactional
    public UserResponse updateProfile(UUID authUserId, UserRequest request) {
        Profile profile = profileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setFullName(request.getFullName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setPhone(request.getPhone());

        Profile saved = profileRepository.save(profile);
        return mapToResponse(saved);
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
