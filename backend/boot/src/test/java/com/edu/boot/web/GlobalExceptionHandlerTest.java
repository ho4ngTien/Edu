package com.edu.boot.web;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {

        record SampleRequest(@NotBlank(message = "Name must not be blank") String name) {}

        @PostMapping("/test/validation")
        void triggerValidation(@Valid @RequestBody SampleRequest request) {}

        @GetMapping("/test/business-error")
        void triggerBusinessError() {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Resource already exists", 409);
        }

        @GetMapping("/test/not-found")
        void triggerNotFound() {
            throw new ResourceNotFoundException("Organization", "org-123");
        }

        @GetMapping("/test/unexpected-error")
        void triggerUnexpected() {
            throw new RuntimeException("Database connection secret detail");
        }
    }

    @Test
    @DisplayName("Nen tra ve 400 Bad Request voi fieldErrors khi validation that bai")
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.fieldErrors.name", is("Name must not be blank")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Nen tra ve 400 Bad Request khi body JSON bi malformed")
    void shouldHandleUnreadableBody() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST_BODY")));
    }

    @Test
    @DisplayName("Nen tra ve dung HTTP status va error code cua BusinessException")
    void shouldHandleBusinessException() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")))
                .andExpect(jsonPath("$.message", is("Resource already exists")));
    }

    @Test
    @DisplayName("Nen tra ve 404 khi nem ResourceNotFoundException")
    void shouldHandleResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Organization not found with identifier: org-123")));
    }

    @Test
    @DisplayName("Nen tra ve 500 va an thong tin nhay cam khi gap loi bat ngo")
    void shouldHandleUnexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected server error occurred")));
    }
}
