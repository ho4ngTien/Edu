package com.edu.academic.infrastructure.web;

import com.edu.academic.api.dto.ClassRoomView;
import com.edu.academic.api.dto.CreateClassRoomRequest;
import com.edu.academic.application.command.CreateClassRoomCommand;
import com.edu.academic.application.service.ClassRoomService;
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
@RequestMapping("/api/v1/organizations/{organizationId}/classes")
public class ClassRoomController {

    private final ClassRoomService classRoomService;

    public ClassRoomController(ClassRoomService classRoomService) {
        this.classRoomService = classRoomService;
    }

    @PostMapping
    public ResponseEntity<ClassRoomView> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateClassRoomRequest request
    ) {
        CreateClassRoomCommand command = new CreateClassRoomCommand(
                request.code(),
                request.name(),
                organizationId,
                request.academicYearId(),
                request.capacity()
        );
        ClassRoomView created = classRoomService.createClassRoom(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClassRoomView>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(classRoomService.listByOrganization(organizationId));
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ClassRoomView> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID classId
    ) {
        return ResponseEntity.ok(classRoomService.getById(organizationId, classId));
    }
}

