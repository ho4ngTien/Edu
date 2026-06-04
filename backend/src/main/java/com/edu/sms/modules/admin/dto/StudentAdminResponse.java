package com.edu.sms.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAdminResponse {
    private UUID id;
    private UUID authUserId;
    private UUID profileId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String studentCode;
    private LocalDate enrollmentDate;
    private boolean active;
}
