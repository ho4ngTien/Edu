package com.edu.sms.modules.academic.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AcademicClassRequest {

    @NotBlank(message = "Tên lớp học không được bỏ trống")
    private String name;

    @NotNull(message = "Mã môn học không được bỏ trống")
    private UUID courseId;

    @NotNull(message = "Mã học kỳ không được bỏ trống")
    private UUID semesterId;

    private UUID teacherId;

    private String room;

    private Integer maxCapacity;
}
