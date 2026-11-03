package com.edu.people.infrastructure.web;

import com.edu.people.api.dto.CreateTeacherRequest;
import com.edu.people.api.dto.TeacherView;
import com.edu.people.application.command.CreateTeacherCommand;
import com.edu.people.application.service.TeacherService;
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
@RequestMapping("/api/v1/organizations/{organizationId}/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping
    public ResponseEntity<TeacherView> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateTeacherRequest request
    ) {
        CreateTeacherCommand command = new CreateTeacherCommand(
                request.userId(),
                request.teacherCode(),
                request.fullName(),
                request.specialization(),
                organizationId
        );
        TeacherView created = teacherService.createTeacher(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TeacherView>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(teacherService.listTeachersByOrganization(organizationId));
    }

    @GetMapping("/{teacherId}")
    public ResponseEntity<TeacherView> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID teacherId
    ) {
        return ResponseEntity.ok(teacherService.getTeacher(organizationId, teacherId));
    }
}

