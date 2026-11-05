package com.edu.people.application.service;

import com.edu.people.api.dto.TeacherView;
import com.edu.people.application.command.CreateTeacherCommand;
import com.edu.people.domain.exception.DuplicateTeacherCodeException;
import com.edu.people.domain.model.TeacherStatus;
import com.edu.people.infrastructure.persistence.entity.TeacherEntity;
import com.edu.people.infrastructure.persistence.repository.TeacherJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TeacherService {

    private final TeacherJpaRepository teacherRepository;

    public TeacherService(TeacherJpaRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public TeacherView createTeacher(CreateTeacherCommand command) {
        String trimmedCode = command.teacherCode().trim();
        if (teacherRepository.existsByTeacherCode(trimmedCode)) {
            throw new DuplicateTeacherCodeException(trimmedCode);
        }

        TeacherEntity entity = new TeacherEntity(
                UUID.randomUUID(),
                command.userId(),
                trimmedCode,
                command.fullName().trim(),
                command.specialization() != null ? command.specialization().trim() : null,
                command.organizationId(),
                TeacherStatus.ACTIVE
        );

        TeacherEntity saved = teacherRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<TeacherView> listTeachersByOrganization(UUID organizationId) {
        return teacherRepository.findAllByOrganizationId(organizationId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherView getTeacher(UUID organizationId, UUID teacherId) {
        return teacherRepository.findByIdAndOrganizationId(teacherId, organizationId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId.toString()));
    }

    private TeacherView toView(TeacherEntity entity) {
        return new TeacherView(
                entity.getId(),
                entity.getUserId(),
                entity.getTeacherCode(),
                entity.getFullName(),
                entity.getSpecialization(),
                entity.getOrganizationId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

