package com.edu.academic.application.service;

import com.edu.academic.api.dto.ClassRoomView;
import com.edu.academic.application.command.CreateClassRoomCommand;
import com.edu.academic.domain.exception.DuplicateClassCodeException;
import com.edu.academic.infrastructure.persistence.entity.ClassRoomEntity;
import com.edu.academic.infrastructure.persistence.repository.AcademicYearJpaRepository;
import com.edu.academic.infrastructure.persistence.repository.ClassRoomJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClassRoomService {

    private final ClassRoomJpaRepository classRoomRepository;
    private final AcademicYearJpaRepository academicYearRepository;

    public ClassRoomService(ClassRoomJpaRepository classRoomRepository, AcademicYearJpaRepository academicYearRepository) {
        this.classRoomRepository = classRoomRepository;
        this.academicYearRepository = academicYearRepository;
    }

    public ClassRoomView createClassRoom(CreateClassRoomCommand command) {
        // Verify academic year belongs to this organization
        academicYearRepository.findByIdAndOrganizationId(command.academicYearId(), command.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", command.academicYearId().toString()));

        String trimmedCode = command.code().trim();
        if (classRoomRepository.existsByCodeAndOrganizationId(trimmedCode, command.organizationId())) {
            throw new DuplicateClassCodeException(trimmedCode);
        }

        ClassRoomEntity entity = new ClassRoomEntity(
                UUID.randomUUID(),
                trimmedCode,
                command.name().trim(),
                command.organizationId(),
                command.academicYearId(),
                command.capacity()
        );

        ClassRoomEntity saved = classRoomRepository.save(entity);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<ClassRoomView> listByOrganization(UUID organizationId) {
        return classRoomRepository.findAllByOrganizationId(organizationId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClassRoomView getById(UUID organizationId, UUID classId) {
        return classRoomRepository.findByIdAndOrganizationId(classId, organizationId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("ClassRoom", classId.toString()));
    }

    private ClassRoomView toView(ClassRoomEntity entity) {
        return new ClassRoomView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getOrganizationId(),
                entity.getAcademicYearId(),
                entity.getCapacity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

