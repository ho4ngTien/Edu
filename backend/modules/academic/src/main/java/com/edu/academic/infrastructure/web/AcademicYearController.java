package com.edu.academic.infrastructure.web;

import com.edu.academic.api.dto.AcademicYearView;
import com.edu.academic.api.dto.CreateAcademicYearRequest;
import com.edu.academic.api.dto.CreateSemesterRequest;
import com.edu.academic.api.dto.SemesterView;
import com.edu.academic.application.command.CreateAcademicYearCommand;
import com.edu.academic.application.command.CreateSemesterCommand;
import com.edu.academic.application.service.AcademicYearService;
import com.edu.academic.application.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final SemesterService semesterService;

    public AcademicYearController(AcademicYearService academicYearService, SemesterService semesterService) {
        this.academicYearService = academicYearService;
        this.semesterService = semesterService;
    }

    @PostMapping
    public ResponseEntity<AcademicYearView> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateAcademicYearRequest request
    ) {
        CreateAcademicYearCommand command = new CreateAcademicYearCommand(
                request.name(),
                request.startDate(),
                request.endDate(),
                organizationId,
                request.status()
        );
        AcademicYearView created = academicYearService.createAcademicYear(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AcademicYearView>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(academicYearService.listByOrganization(organizationId));
    }

    @GetMapping("/{yearId}")
    public ResponseEntity<AcademicYearView> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID yearId
    ) {
        return ResponseEntity.ok(academicYearService.getById(organizationId, yearId));
    }

    @PostMapping("/{yearId}/semesters")
    public ResponseEntity<SemesterView> createSemester(
            @PathVariable UUID organizationId,
            @PathVariable UUID yearId,
            @Valid @RequestBody CreateSemesterRequest request
    ) {
        CreateSemesterCommand command = new CreateSemesterCommand(
                yearId,
                request.name(),
                request.startDate(),
                request.endDate(),
                organizationId
        );
        SemesterView created = semesterService.createSemester(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{yearId}/semesters")
    public ResponseEntity<List<SemesterView>> listSemesters(
            @PathVariable UUID organizationId,
            @PathVariable UUID yearId
    ) {
        return ResponseEntity.ok(semesterService.listByAcademicYear(organizationId, yearId));
    }
}

