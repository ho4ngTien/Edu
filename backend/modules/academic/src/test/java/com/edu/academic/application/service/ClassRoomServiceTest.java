package com.edu.academic.application.service;

import com.edu.academic.api.dto.ClassRoomView;
import com.edu.academic.application.command.CreateClassRoomCommand;
import com.edu.academic.domain.exception.DuplicateClassCodeException;
import com.edu.academic.infrastructure.persistence.entity.AcademicYearEntity;
import com.edu.academic.infrastructure.persistence.entity.ClassRoomEntity;
import com.edu.academic.infrastructure.persistence.repository.AcademicYearJpaRepository;
import com.edu.academic.infrastructure.persistence.repository.ClassRoomJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassRoomServiceTest {

    @Mock
    private ClassRoomJpaRepository classRoomRepository;

    @Mock
    private AcademicYearJpaRepository academicYearRepository;

    private ClassRoomService service;

    @BeforeEach
    void setUp() {
        service = new ClassRoomService(classRoomRepository, academicYearRepository);
    }

    @Test
    void createClassRoom_whenAcademicYearNotFound_throwsResourceNotFoundException() {
        UUID orgId = UUID.randomUUID();
        UUID yearId = UUID.randomUUID();
        when(academicYearRepository.findByIdAndOrganizationId(yearId, orgId)).thenReturn(Optional.empty());

        CreateClassRoomCommand command = new CreateClassRoomCommand(
                "12A1", "Lop 12A1", orgId, yearId, 35
        );

        assertThatThrownBy(() -> service.createClassRoom(command))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createClassRoom_whenCodeAlreadyExists_throwsDuplicateClassCodeException() {
        UUID orgId = UUID.randomUUID();
        UUID yearId = UUID.randomUUID();
        when(academicYearRepository.findByIdAndOrganizationId(yearId, orgId))
                .thenReturn(Optional.of(new AcademicYearEntity(yearId, "2024-2025", LocalDate.now(), LocalDate.now().plusMonths(9), orgId, null)));
        when(classRoomRepository.existsByCodeAndOrganizationId("12A1", orgId)).thenReturn(true);

        CreateClassRoomCommand command = new CreateClassRoomCommand(
                "12A1", "Lop 12A1", orgId, yearId, 35
        );

        assertThatThrownBy(() -> service.createClassRoom(command))
                .isInstanceOf(DuplicateClassCodeException.class);
    }

    @Test
    void createClassRoom_success_savesAndReturnsView() {
        UUID orgId = UUID.randomUUID();
        UUID yearId = UUID.randomUUID();
        when(academicYearRepository.findByIdAndOrganizationId(yearId, orgId))
                .thenReturn(Optional.of(new AcademicYearEntity(yearId, "2024-2025", LocalDate.now(), LocalDate.now().plusMonths(9), orgId, null)));
        when(classRoomRepository.existsByCodeAndOrganizationId("12A1", orgId)).thenReturn(false);
        when(classRoomRepository.save(any(ClassRoomEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateClassRoomCommand command = new CreateClassRoomCommand(
                "12A1", "Lop 12A1", orgId, yearId, 35
        );

        ClassRoomView result = service.createClassRoom(command);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("12A1");
        assertThat(result.capacity()).isEqualTo(35);
        verify(classRoomRepository).save(any(ClassRoomEntity.class));
    }
}

