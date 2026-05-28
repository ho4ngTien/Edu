package com.edu.sms.modules.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {
    private UUID studentId;
    private UUID assignmentId;
    private BigDecimal score;
    private String feedback;
}
