package com.edu.sms.modules.grade.service;

import com.edu.sms.modules.grade.dto.GradeRequest;
import com.edu.sms.modules.grade.dto.GradeResponse;
import com.edu.sms.modules.grade.model.Grade;
import com.edu.sms.modules.grade.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;

    @Transactional
    public GradeResponse submitGrade(GradeRequest request, UUID gradedBy) {
        Grade grade = Grade.builder()
                .studentId(request.getStudentId())
                .assignmentId(request.getAssignmentId())
                .score(request.getScore())
                .feedback(request.getFeedback())
                .gradedBy(gradedBy)
                .build();
        Grade saved = gradeRepository.save(grade);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesForStudent(UUID studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private GradeResponse mapToResponse(Grade grade) {
        return GradeResponse.builder()
                .id(grade.getId())
                .studentId(grade.getStudentId())
                .assignmentId(grade.getAssignmentId())
                .score(grade.getScore())
                .feedback(grade.getFeedback())
                .build();
    }
}
