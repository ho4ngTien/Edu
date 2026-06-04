package com.edu.sms.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicClassResponse {
    private UUID id;
    private String name;
    private UUID courseId;
    private String courseName;
    private UUID semesterId;
    private String semesterName;
    private UUID teacherId;
    private String teacherName;
    private String room;
    private Integer maxCapacity;
}
