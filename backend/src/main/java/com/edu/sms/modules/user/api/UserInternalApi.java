package com.edu.sms.modules.user.api;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface UserInternalApi {
    String getTeacherName(UUID teacherId);
    boolean existsStudent(UUID studentId);
    boolean existsTeacher(UUID teacherId);
    Map<UUID, String> getStudentNames(Set<UUID> studentIds);
    UUID getStudentIdByAuthUserId(UUID authUserId);
    UUID getTeacherIdByAuthUserId(UUID authUserId);
}
