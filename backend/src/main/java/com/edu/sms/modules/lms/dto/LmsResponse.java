package com.edu.sms.modules.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LmsResponse {
    private UUID id;
    private UUID classId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
}
