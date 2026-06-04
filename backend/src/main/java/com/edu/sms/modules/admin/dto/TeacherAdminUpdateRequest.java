package com.edu.sms.modules.admin.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherAdminUpdateRequest {
    private String fullName;
    private String phone;
    private String teacherCode;
    private String department;
    private LocalDate hireDate;

    @Email(message = "Email không hợp lệ")
    private String email;

    private Boolean active;
}
