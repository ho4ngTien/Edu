package com.edu.people.application.service;

import com.edu.people.api.dto.TeacherView;
import com.edu.people.application.command.CreateTeacherCommand;
import com.edu.people.domain.exception.DuplicateTeacherCodeException;
import com.edu.people.domain.model.TeacherStatus;
import com.edu.people.infrastructure.persistence.entity.TeacherEntity;
import com.edu.people.infrastructure.persistence.repository.TeacherJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherJpaRepository teacherRepository;

    private TeacherService teacherService;

    @BeforeEach
    void setUp() {
        teacherService = new TeacherService(teacherRepository);
    }

    @Test
    void createTeacher_whenCodeAlreadyExists_throwsDuplicateTeacherCodeException() {
        when(teacherRepository.existsByTeacherCode("TCH001")).thenReturn(true);

        CreateTeacherCommand command = new CreateTeacherCommand(
                UUID.randomUUID(),
                "TCH001",
                "Tran Thi B",
                "Computer Science",
                UUID.randomUUID()
        );

        assertThatThrownBy(() -> teacherService.createTeacher(command))
                .isInstanceOf(DuplicateTeacherCodeException.class);
    }

    @Test
    void createTeacher_success_savesAndReturnsView() {
        when(teacherRepository.existsByTeacherCode("TCH001")).thenReturn(false);
        when(teacherRepository.save(any(TeacherEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID orgId = UUID.randomUUID();
        CreateTeacherCommand command = new CreateTeacherCommand(
                UUID.randomUUID(),
                "TCH001",
                "Tran Thi B",
                "Computer Science",
                orgId
        );

        TeacherView result = teacherService.createTeacher(command);

        assertThat(result).isNotNull();
        assertThat(result.teacherCode()).isEqualTo("TCH001");
        assertThat(result.status()).isEqualTo(TeacherStatus.ACTIVE);
        verify(teacherRepository).save(any(TeacherEntity.class));
    }

    @Test
    void getTeacher_whenNotFound_throwsResourceNotFoundException() {
        UUID orgId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        when(teacherRepository.findByIdAndOrganizationId(teacherId, orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getTeacher(orgId, teacherId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

