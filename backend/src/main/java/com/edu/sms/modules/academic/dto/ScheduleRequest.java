package com.edu.sms.modules.academic.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "Thứ không được bỏ trống")
    @Min(value = 1, message = "Thứ phải từ 1 đến 7")
    @Max(value = 7, message = "Thứ phải từ 1 đến 7")
    private Integer dayOfWeek;

    @NotNull(message = "Thời gian bắt đầu không được bỏ trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Thời gian kết thúc không được bỏ trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "Phòng học không được bỏ trống")
    private String room;
}
