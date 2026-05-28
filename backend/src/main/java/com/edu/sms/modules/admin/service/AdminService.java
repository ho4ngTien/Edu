package com.edu.sms.modules.admin.service;

import com.edu.sms.modules.admin.dto.AdminRequest;
import com.edu.sms.modules.admin.dto.AdminResponse;
import com.edu.sms.modules.admin.model.SystemConfig;
import com.edu.sms.modules.admin.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    public AdminResponse getConfig(String configId) {
        SystemConfig config = systemConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Config not found"));
        return mapToResponse(config);
    }

    @Transactional
    public AdminResponse updateConfig(String configId, AdminRequest request, UUID updatedBy) {
        SystemConfig config = systemConfigRepository.findById(configId)
                .orElse(SystemConfig.builder().id(configId).build());

        config.setValue(request.getValue());
        config.setDescription(request.getDescription());
        config.setUpdatedBy(updatedBy);

        SystemConfig saved = systemConfigRepository.save(config);
        return mapToResponse(saved);
    }

    private AdminResponse mapToResponse(SystemConfig config) {
        return AdminResponse.builder()
                .id(config.getId())
                .value(config.getValue())
                .description(config.getDescription())
                .build();
    }
}
