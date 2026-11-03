package com.edu.people.application.service;

import com.edu.people.api.dto.StudentView;
import com.edu.people.application.command.CreateStudentCommand;
import com.edu.people.domain.exception.DuplicateStudentCodeException;
import com.edu.people.domain.model.StudentStatus;
import com.edu.people.infrastructure.persistence.entity.StudentEntity;
import com.edu.people.infrastructure.persistence.repository.StudentJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentService {

    private final StudentJpaRepository studentRepository;

    public StudentService(StudentJpaRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentView createStudent(CreateStudentCommand command) {
        String trimmedCode = command.studentCode().trim();
        if (studentRepository.existsByStudentCode(trimmedCode)) {
            throw new DuplicateStudentCodeException(trimmedCode);
        }

        StudentEntity entity = new StudentEntity(
                UUID.randomUUID(),
                command.userId(),
                trimmedCode,
                command.fullName().trim(),
                command.dateOfBirth(),
                command.organizationId(),
                StudentStatus.ACTIVE,
                command.enrolledAt()
        );

        StudentEntity saved = studentRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentView> listStudentsByOrganization(UUID organizationId) {
        return studentRepository.findAllByOrganizationId(organizationId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentView getStudent(UUID organizationId, UUID studentId) {
        return studentRepository.findByIdAndOrganizationId(studentId, organizationId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId.toString()));
    }

    public StudentView deactivateStudent(UUID organizationId, UUID studentId) {
        StudentEntity entity = studentRepository.findByIdAndOrganizationId(studentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId.toString()));
        entity.setStatus(StudentStatus.INACTIVE);
        return toView(entity);
    }

    private StudentView toView(StudentEntity entity) {
        return new StudentView(
                entity.getId(),
                entity.getUserId(),
                entity.getStudentCode(),
                entity.getFullName(),
                entity.getDateOfBirth(),
                entity.getOrganizationId(),
                entity.getStatus(),
                entity.getEnrolledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

