package com.edu.boot.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors,
        String traceId
) {
    public ApiErrorResponse(
            Instant timestamp,
            int status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        this(timestamp, status, code, message, path, fieldErrors, MDC.get("traceId"));
    }

    public static ApiErrorResponse of(
            int status,
            String code,
            String message,
            String path
    ) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, Map.of(), MDC.get("traceId"));
    }

    public static ApiErrorResponse ofValidation(
            int status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, fieldErrors, MDC.get("traceId"));
    }
}
