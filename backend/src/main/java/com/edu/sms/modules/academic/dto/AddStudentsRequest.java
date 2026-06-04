package com.edu.sms.modules.academic.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddStudentsRequest {

    @NotEmpty(message = "Danh sách học sinh không được bỏ trống")
    private Set<UUID> studentIds;
}
