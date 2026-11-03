package com.edu.curriculum.application.service;

import com.edu.curriculum.api.dto.SubjectView;
import com.edu.curriculum.application.command.CreateSubjectCommand;
import com.edu.curriculum.domain.exception.DuplicateSubjectCodeException;
import com.edu.curriculum.domain.model.SubjectStatus;
import com.edu.curriculum.infrastructure.persistence.entity.SubjectEntity;
import com.edu.curriculum.infrastructure.persistence.repository.SubjectJpaRepository;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectJpaRepository repository;

    private SubjectService service;

    @BeforeEach
    void setUp() {
        service = new SubjectService(repository);
    }

    @Test
    void createSubject_whenCodeAlreadyExists_throwsDuplicateSubjectCodeException() {
        UUID orgId = UUID.randomUUID();
        when(repository.existsByCodeAndOrganizationId("MATH101", orgId)).thenReturn(true);

        CreateSubjectCommand command = new CreateSubjectCommand(
                "MATH101", "Toan Dai Cuong", "Mo ta", 3, orgId
        );

        assertThatThrownBy(() -> service.createSubject(command))
                .isInstanceOf(DuplicateSubjectCodeException.class);
    }

    @Test
    void createSubject_success_savesAndReturnsView() {
        UUID orgId = UUID.randomUUID();
        when(repository.existsByCodeAndOrganizationId("MATH101", orgId)).thenReturn(false);
        when(repository.save(any(SubjectEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateSubjectCommand command = new CreateSubjectCommand(
                "MATH101", "Toan Dai Cuong", "Mo ta", 3, orgId
        );

        SubjectView result = service.createSubject(command);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("MATH101");
        assertThat(result.status()).isEqualTo(SubjectStatus.ACTIVE);
        verify(repository).save(any(SubjectEntity.class));
    }

    @Test
    void deprecateSubject_whenFound_setsDeprecated() {
        UUID orgId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        SubjectEntity entity = new SubjectEntity(
                subjectId, "MATH101", "Toan Dai Cuong", "Mo ta", 3, orgId, SubjectStatus.ACTIVE
        );
        when(repository.findByIdAndOrganizationId(subjectId, orgId)).thenReturn(Optional.of(entity));

        SubjectView result = service.deprecateSubject(orgId, subjectId);

        assertThat(result.status()).isEqualTo(SubjectStatus.DEPRECATED);
    }

    @Test
    void getById_whenNotFound_throwsResourceNotFoundException() {
        UUID orgId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        when(repository.findByIdAndOrganizationId(subjectId, orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(orgId, subjectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

