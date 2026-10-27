package com.edu.boot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class RestSecurityExceptionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RestSecurityExceptionHandler handler = new RestSecurityExceptionHandler(objectMapper);

    @Test
    @DisplayName("commence writes 401 JSON ApiErrorResponse")
    void commenceReturns401Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).contains("\"status\":401");
        assertThat(response.getContentAsString()).contains("\"code\":\"UNAUTHORIZED\"");
        assertThat(response.getContentAsString()).contains("\"path\":\"/api/v1/protected\"");
    }

    @Test
    @DisplayName("handle writes 403 JSON ApiErrorResponse")
    void handleReturns403Json() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).contains("\"status\":403");
        assertThat(response.getContentAsString()).contains("\"code\":\"FORBIDDEN\"");
        assertThat(response.getContentAsString()).contains("\"path\":\"/api/v1/admin\"");
    }
}
