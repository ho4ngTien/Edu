package com.edu.sms.modules.academic.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassStudentId implements Serializable {
    private UUID classId;
    private UUID studentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassStudentId that = (ClassStudentId) o;
        return Objects.equals(classId, that.classId) && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, studentId);
    }
}
