package com.edu.sms.modules.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Mã môn học không được bỏ trống")
    private String code;

    @NotBlank(message = "Tên môn học không được bỏ trống")
    private String name;

    private String description;

    @NotNull(message = "Số tín chỉ không được bỏ trống")
    @Min(value = 0, message = "Số tín chỉ phải lớn hơn hoặc bằng 0")
    private Integer credits;

    private Boolean isActive;
}
