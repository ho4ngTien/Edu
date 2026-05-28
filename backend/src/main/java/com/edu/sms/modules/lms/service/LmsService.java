package com.edu.sms.modules.lms.service;

import com.edu.sms.modules.lms.dto.LmsRequest;
import com.edu.sms.modules.lms.dto.LmsResponse;
import com.edu.sms.modules.lms.model.Assignment;
import com.edu.sms.modules.lms.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LmsService {

    private final AssignmentRepository assignmentRepository;

    @Transactional
    public LmsResponse createAssignment(UUID classId, LmsRequest request, UUID createdBy) {
        Assignment assignment = Assignment.builder()
                .classId(classId)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .createdBy(createdBy)
                .build();
        Assignment saved = assignmentRepository.save(assignment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LmsResponse> getAssignmentsByClass(UUID classId) {
        return assignmentRepository.findByClassId(classId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LmsResponse mapToResponse(Assignment assignment) {
        return LmsResponse.builder()
                .id(assignment.getId())
                .classId(assignment.getClassId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .build();
    }
}
