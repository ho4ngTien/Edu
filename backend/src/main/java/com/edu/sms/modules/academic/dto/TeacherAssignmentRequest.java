package com.edu.sms.modules.academic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentRequest {
    @NotNull(message = "Mã giáo viên không được bỏ trống")
    private UUID teacherId;
}
