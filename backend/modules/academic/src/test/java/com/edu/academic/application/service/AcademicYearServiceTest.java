package com.edu.academic.application.service;

import com.edu.academic.api.dto.AcademicYearView;
import com.edu.academic.application.command.CreateAcademicYearCommand;
import com.edu.academic.domain.exception.InvalidAcademicPeriodException;
import com.edu.academic.domain.model.AcademicStatus;
import com.edu.academic.infrastructure.persistence.entity.AcademicYearEntity;
import com.edu.academic.infrastructure.persistence.repository.AcademicYearJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

    @Mock
    private AcademicYearJpaRepository repository;

    private AcademicYearService service;

    @BeforeEach
    void setUp() {
        service = new AcademicYearService(repository);
    }

    @Test
    void createAcademicYear_whenEndDateBeforeStartDate_throwsInvalidAcademicPeriodException() {
        CreateAcademicYearCommand command = new CreateAcademicYearCommand(
                "2024-2025",
                LocalDate.of(2025, 6, 30),
                LocalDate.of(2024, 9, 1),
                UUID.randomUUID(),
                AcademicStatus.UPCOMING
        );

        assertThatThrownBy(() -> service.createAcademicYear(command))
                .isInstanceOf(InvalidAcademicPeriodException.class);
    }

    @Test
    void createAcademicYear_whenDatesEqual_throwsInvalidAcademicPeriodException() {
        CreateAcademicYearCommand command = new CreateAcademicYearCommand(
                "2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 1),
                UUID.randomUUID(),
                AcademicStatus.UPCOMING
        );

        assertThatThrownBy(() -> service.createAcademicYear(command))
                .isInstanceOf(InvalidAcademicPeriodException.class);
    }

    @Test
    void createAcademicYear_success_savesAndReturnsView() {
        when(repository.save(any(AcademicYearEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID orgId = UUID.randomUUID();
        CreateAcademicYearCommand command = new CreateAcademicYearCommand(
                "2024-2025",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2025, 6, 30),
                orgId,
                AcademicStatus.ACTIVE
        );

        AcademicYearView result = service.createAcademicYear(command);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("2024-2025");
        assertThat(result.status()).isEqualTo(AcademicStatus.ACTIVE);
        verify(repository).save(any(AcademicYearEntity.class));
    }
}

