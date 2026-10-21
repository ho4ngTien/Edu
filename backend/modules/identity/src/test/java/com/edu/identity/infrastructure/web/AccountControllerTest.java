package com.edu.identity.infrastructure.web;

import com.edu.identity.api.dto.AccountView;
import com.edu.identity.application.command.ChangeAccountStatusCommand;
import com.edu.identity.application.command.RegisterAccountCommand;
import com.edu.identity.application.service.IdentityService;
import com.edu.identity.domain.exception.DuplicateUsernameException;
import com.edu.identity.domain.model.UserStatus;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private IdentityService identityService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountController(identityService))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/identity/accounts registers new account and returns 201")
    void registerAccountSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        AccountView view = new AccountView(userId, "student1", "student1@edu.vn", UserStatus.ACTIVE);

        given(identityService.registerAccount(any(RegisterAccountCommand.class))).willReturn(view);

        String json = """
                {
                    "username": "student1",
                    "email": "student1@edu.vn",
                    "password": "StrongPassword123"
                }
                """;

        mockMvc.perform(post("/api/v1/identity/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("student1"))
                .andExpect(jsonPath("$.email").value("student1@edu.vn"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.rawPassword").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/identity/accounts returns 400 on invalid input")
    void registerAccountValidationFailure() throws Exception {
        String json = """
                {
                    "username": "a",
                    "email": "not-an-email",
                    "password": "short"
                }
                """;

        mockMvc.perform(post("/api/v1/identity/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/identity/accounts/{id} returns 200 with AccountView")
    void getAccountById() throws Exception {
        UUID userId = UUID.randomUUID();
        AccountView view = new AccountView(userId, "student1", "student1@edu.vn", UserStatus.ACTIVE);

        given(identityService.getAccount(userId)).willReturn(view);

        mockMvc.perform(get("/api/v1/identity/accounts/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("student1"));
    }

    @Test
    @DisplayName("PATCH /api/v1/identity/accounts/{id}/status returns updated account")
    void changeAccountStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        AccountView view = new AccountView(userId, "student1", "student1@edu.vn", UserStatus.LOCKED);

        given(identityService.changeAccountStatus(any(ChangeAccountStatusCommand.class))).willReturn(view);

        String json = """
                {
                    "status": "LOCKED"
                }
                """;

        mockMvc.perform(patch("/api/v1/identity/accounts/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }
}
