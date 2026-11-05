package com.edu.curriculum.infrastructure.web;

import com.edu.curriculum.api.dto.CreateSubjectRequest;
import com.edu.curriculum.api.dto.SubjectView;
import com.edu.curriculum.application.command.CreateSubjectCommand;
import com.edu.curriculum.application.service.SubjectService;
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
@RequestMapping("/api/v1/organizations/{organizationId}/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<SubjectView> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        CreateSubjectCommand command = new CreateSubjectCommand(
                request.code(),
                request.name(),
                request.description(),
                request.credits(),
                organizationId
        );
        SubjectView created = subjectService.createSubject(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SubjectView>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(subjectService.listByOrganization(organizationId));
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<SubjectView> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID subjectId
    ) {
        return ResponseEntity.ok(subjectService.getById(organizationId, subjectId));
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<SubjectView> deprecate(
            @PathVariable UUID organizationId,
            @PathVariable UUID subjectId
    ) {
        return ResponseEntity.ok(subjectService.deprecateSubject(organizationId, subjectId));
    }
}

