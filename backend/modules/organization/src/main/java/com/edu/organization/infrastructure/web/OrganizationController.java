package com.edu.organization.infrastructure.web;

import com.edu.organization.api.dto.CreateOrganizationRequest;
import com.edu.organization.api.dto.OrganizationView;
import com.edu.organization.api.dto.UpdateOrganizationRequest;
import com.edu.organization.application.command.CreateOrganizationCommand;
import com.edu.organization.application.command.UpdateOrganizationCommand;
import com.edu.organization.application.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationView> create(@Valid @RequestBody CreateOrganizationRequest request) {
        CreateOrganizationCommand command = new CreateOrganizationCommand(
                request.code(),
                request.name()
        );
        OrganizationView created = organizationService.createOrganization(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<OrganizationView> getById(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(organizationService.getOrganization(organizationId));
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<OrganizationView> update(
            @PathVariable UUID organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        UpdateOrganizationCommand command = new UpdateOrganizationCommand(request.name());
        return ResponseEntity.ok(organizationService.updateOrganization(organizationId, command));
    }
}
