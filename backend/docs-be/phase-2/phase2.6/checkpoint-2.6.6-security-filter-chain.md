# Phase 2.6: Checkpoint 2.6.6 — Stateless Security Filter Chain & Error Standardization

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 9. Checkpoint 2.6.6: Stateless Security Filter Chain & Error Standardization

Mục tiêu của checkpoint 2.6.6 là hoàn thiện cấu hình `SecurityFilterChain` của Spring Security 6, cấu hình chế độ Stateless, phân loại chính xác các endpoints công khai/bảo vệ, và ghi đè xử lý lỗi 401/403 theo định dạng `ApiErrorResponse`.

### 9.1. Chuẩn hóa Lỗi Bảo mật: AuthenticationEntryPoint & AccessDeniedHandler

Vị trí: `backend/boot/src/main/java/com/edu/boot/security/RestSecurityExceptionHandler.java`

```java
package com.edu.boot.security;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import com.edu.shared.kernel.api.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RestSecurityExceptionHandler(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        writeErrorResponse(
            response,
            request.getRequestURI(),
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "Full authentication is required to access this resource"
        );
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        writeErrorResponse(
            response,
            request.getRequestURI(),
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "Access denied: You do not have permission to access this resource"
        );
    }

    private void writeErrorResponse(
        HttpServletResponse response,
        String path,
        HttpStatus status,
        String errorCode,
        String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            Instant.now(clock),
            status.value(),
            errorCode,
            message,
            path,
            null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
```

---

### 9.2. Cấu hình SecurityFilterChain

Vị trí: `backend/boot/src/main/java/com/edu/boot/security/SecurityConfig.java`

```java
package com.edu.boot.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RestSecurityExceptionHandler restSecurityExceptionHandler;
    private final JwtDecoder jwtDecoder;

    public SecurityConfig(
        RestSecurityExceptionHandler restSecurityExceptionHandler,
        JwtDecoder jwtDecoder
    ) {
        this.restSecurityExceptionHandler = restSecurityExceptionHandler;
        this.jwtDecoder = jwtDecoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(restSecurityExceptionHandler)
                .accessDeniedHandler(restSecurityExceptionHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints công khai
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/identity/accounts/register").permitAll()
                
                // Actuator Health & Info
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Mọi request còn lại bắt buộc phải xác thực Bearer Token
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder))
                .authenticationEntryPoint(restSecurityExceptionHandler)
                .accessDeniedHandler(restSecurityExceptionHandler)
            )
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:[*]"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Organization-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

### 9.3. Tiêu chí Hoàn thành Checkpoint 2.6.6

| ID | Tiêu chí | Trạng thái |
| :--- | :--- | :---: |
| **AC-2.6.6.1** | Session policy cấu hình `STATELESS`; không tạo JSESSIONID cookie | **DONE** |
| **AC-2.6.6.2** | Endpoints `/api/v1/auth/*` và đăng ký tài khoản cho phép truy cập public | **DONE** |
| **AC-2.6.6.3** | Toàn bộ API nghiệp vụ còn lại yêu cầu Bearer JWT hợp lệ | **DONE** |
| **AC-2.6.6.4** | Lỗi 401 khi thiếu/hết hạn token trả về đúng JSON `ApiErrorResponse` | **DONE** |
| **AC-2.6.6.5** | Lỗi 403 khi thiếu quyền trả về đúng JSON `ApiErrorResponse` | **DONE** |
| **AC-2.6.6.6** | Cấu hình CORS chấp nhận header `X-Organization-Id` và method credentials | **DONE** |

---

