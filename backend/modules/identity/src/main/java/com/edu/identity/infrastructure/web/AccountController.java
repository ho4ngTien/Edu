package com.edu.identity.infrastructure.web;

import com.edu.identity.api.dto.AccountView;
import com.edu.identity.api.dto.ChangeAccountStatusRequest;
import com.edu.identity.api.dto.RegisterAccountRequest;
import com.edu.identity.application.command.ChangeAccountStatusCommand;
import com.edu.identity.application.command.RegisterAccountCommand;
import com.edu.identity.application.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountView> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeAccountStatusRequest request
    ) {
        ChangeAccountStatusCommand command = new ChangeAccountStatusCommand(id, request.status());
        return ResponseEntity.ok(identityService.changeAccountStatus(command));
    }
}
