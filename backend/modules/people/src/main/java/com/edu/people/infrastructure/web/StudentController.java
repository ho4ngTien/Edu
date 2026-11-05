package com.edu.people.infrastructure.web;

import com.edu.people.api.dto.CreateStudentRequest;
import com.edu.people.api.dto.StudentView;
import com.edu.people.application.command.CreateStudentCommand;
import com.edu.people.application.service.StudentService;
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
@RequestMapping("/api/v1/organizations/{organizationId}/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentView> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateStudentRequest request
    ) {
        CreateStudentCommand command = new CreateStudentCommand(
                request.userId(),
                request.studentCode(),
                request.fullName(),
                request.dateOfBirth(),
                organizationId,
                request.enrolledAt()
        );
        StudentView created = studentService.createStudent(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<StudentView>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(studentService.listStudentsByOrganization(organizationId));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentView> getById(
            @PathVariable UUID organizationId,
            @PathVariable UUID studentId
    ) {
        return ResponseEntity.ok(studentService.getStudent(organizationId, studentId));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<StudentView> deactivate(
            @PathVariable UUID organizationId,
            @PathVariable UUID studentId
    ) {
        return ResponseEntity.ok(studentService.deactivateStudent(organizationId, studentId));
    }
}

