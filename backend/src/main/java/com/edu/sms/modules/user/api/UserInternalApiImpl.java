package com.edu.sms.modules.user.api;

import com.edu.sms.common.exception.ResourceNotFoundException;
import com.edu.sms.modules.user.model.Student;
import com.edu.sms.modules.user.model.Teacher;
import com.edu.sms.modules.user.repository.StudentRepository;
import com.edu.sms.modules.user.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserInternalApiImpl implements UserInternalApi {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Override
    public String getTeacherName(UUID teacherId) {
        return teacherRepository.findById(teacherId)
                .map(t -> t.getProfile().getFullName())
                .orElse("Unknown Teacher");
    }

    @Override
    public boolean existsStudent(UUID studentId) {
        return studentRepository.existsById(studentId);
    }

    @Override
    public boolean existsTeacher(UUID teacherId) {
        return teacherRepository.existsById(teacherId);
    }

    @Override
    public Map<UUID, String> getStudentNames(Set<UUID> studentIds) {
        return studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(
                        Student::getId,
                        s -> s.getProfile().getFullName()
                ));
    }

    @Override
    public UUID getStudentIdByAuthUserId(UUID authUserId) {
        return studentRepository.findByProfileAuthUserId(authUserId)
                .map(Student::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Học sinh không tồn tại"));
    }

    @Override
    public UUID getTeacherIdByAuthUserId(UUID authUserId) {
        return teacherRepository.findByProfileAuthUserId(authUserId)
                .map(Teacher::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Giáo viên không tồn tại"));
    }
}
