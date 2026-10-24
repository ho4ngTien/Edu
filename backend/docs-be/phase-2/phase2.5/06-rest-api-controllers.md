# Phase 2.5: REST API Controllers & DTOs

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.6. REST API Controllers & DTOs

#### 1. API Đăng ký Tài khoản: `POST /api/v1/identity/accounts`

**Request DTO:**
```java
package com.edu.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterAccountRequest(
        @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, and hyphens")
        String username,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password
) {}
```

**Controller:**
```java
package com.edu.identity.infrastructure.web;

import com.edu.identity.api.dto.AccountView;
import com.edu.identity.api.dto.RegisterAccountRequest;
import com.edu.identity.application.command.RegisterAccountCommand;
import com.edu.identity.application.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity/accounts")
public class AccountController {

    private final IdentityService identityService;

    public AccountController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping
    public ResponseEntity<AccountView> register(@Valid @RequestBody RegisterAccountRequest request) {
        RegisterAccountCommand command = new RegisterAccountCommand(
                request.username(),
                request.email(),
                request.password()
        );
        AccountView created = identityService.registerAccount(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountView> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(identityService.getAccount(id));
    }
}
```

#### 2. API Quản lý Tổ chức & Thành viên

**Controller:**
```java
package com.edu.organization.infrastructure.web;

import com.edu.organization.api.dto.CreateOrganizationRequest;
import com.edu.organization.api.dto.OrganizationView;
import com.edu.organization.application.command.CreateOrganizationCommand;
import com.edu.organization.application.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}
```

---
