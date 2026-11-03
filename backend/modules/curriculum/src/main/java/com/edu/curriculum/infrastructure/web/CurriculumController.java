package com.edu.curriculum.infrastructure.web;

import com.edu.curriculum.api.dto.AssignSubjectRequest;
import com.edu.curriculum.api.dto.CreateCurriculumRequest;
import com.edu.curriculum.api.dto.CurriculumSubjectView;
import com.edu.curriculum.api.dto.CurriculumView;
import com.edu.curriculum.application.command.AssignSubjectCommand;
import com.edu.curriculum.application.command.CreateCurriculumCommand;
import com.edu.curriculum.application.service.CurriculumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/curricula")
public class CurriculumController {

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @PostMapping
    public ResponseEntity<CurriculumView> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateCurriculumRequest request
    ) {
        CreateCurriculumCommand command = new CreateCurriculumCommand(
                request.code(),
                request.name(),
                request.description(),
                request.totalCredits(),
                organizationId
        );
        CurriculumView created = curriculumService.createCurriculum(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CurriculumView>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(curriculumService.listByOrganization(organizationId));
    }

    @GetMapping("/{curriculumId}")
    public ResponseEntity<CurriculumView> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID curriculumId
    ) {
        return ResponseEntity.ok(curriculumService.getById(organizationId, curriculumId));
    }

    @PostMapping("/{curriculumId}/subjects")
    public ResponseEntity<CurriculumSubjectView> assignSubject(
            @PathVariable UUID organizationId,
            @PathVariable UUID curriculumId,
            @Valid @RequestBody AssignSubjectRequest request
    ) {
        AssignSubjectCommand command = new AssignSubjectCommand(
                curriculumId,
                request.subjectId(),
                organizationId,
                request.isRequired(),
                request.sequenceOrder()
        );
        CurriculumSubjectView created = curriculumService.assignSubject(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{curriculumId}/subjects")
    public ResponseEntity<List<CurriculumSubjectView>> listSubjects(
            @PathVariable UUID organizationId,
            @PathVariable UUID curriculumId
    ) {
        return ResponseEntity.ok(curriculumService.listSubjects(organizationId, curriculumId));
    }

    @DeleteMapping("/{curriculumId}/subjects/{subjectId}")
    public ResponseEntity<Void> removeSubject(
            @PathVariable UUID organizationId,
            @PathVariable UUID curriculumId,
            @PathVariable UUID subjectId
    ) {
        curriculumService.removeSubject(organizationId, curriculumId, subjectId);
        return ResponseEntity.noContent().build();
    }
}

