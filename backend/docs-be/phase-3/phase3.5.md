# Phase 3.5 — Spring Boot Actuator, Metrics & Health Indicators (Prometheus)

> **Dự án:** E-Edu Backend · Java 21 + Spring Boot 3.4 · Modular Monolith  
> **Trạng thái:** 🚧 Đang triển khai

---

## 1. Mục tiêu

Triển khai **observability layer** cho hệ thống E-Edu, bao gồm:

| Mục tiêu | Công cụ |
|---|---|
| Health check cho orchestration (K8s, Docker) | Spring Boot Actuator `/actuator/health` |
| Thu thập metrics runtime | Micrometer + Prometheus |
| Custom health check cho multi-schema DB | `HealthIndicator` tùy chỉnh |
| Bảo vệ sensitive endpoints | Spring Security + actuator security config |
| Dashboard giám sát | Grafana kết nối Prometheus |

---

## 2. Dependencies — `pom.xml`

```xml
<!-- Spring Boot Actuator — health, info, metrics endpoints -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Micrometer Prometheus registry — export metrics theo format Prometheus -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Micrometer Tracing (tùy chọn — OpenTelemetry bridge) -->
<!--
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
-->
```

---

## 3. Cấu hình Actuator trong `application.yml`

```yaml
management:
  # ─── Endpoints Configuration ─────────────────────────────────────────────
  endpoints:
    web:
      # Expose các endpoint qua HTTP
      exposure:
        include:
          - health
          - info
          - metrics
          - prometheus
          - loggers      # Thay đổi log level runtime (cần bảo vệ)
          - env          # Xem biến môi trường (NHẠY CẢM — chỉ internal)
          - flyway       # Trạng thái Flyway migrations
          - threaddump   # Thread dump (debug)
          - heapdump     # Heap dump (debug, cần bảo vệ)
      base-path: /actuator  # Prefix mặc định

  # ─── Health Endpoint ────────────────────────────────────────────────────
  endpoint:
    health:
      # Hiển thị chi tiết health (UP/DOWN của từng component)
      show-details: when-authorized  # always | never | when-authorized
      show-components: when-authorized
      # Bao gồm custom health indicators
      probes:
        enabled: true  # Kubernetes liveness/readiness probes

  # ─── Info Endpoint ───────────────────────────────────────────────────────
  info:
    env:
      enabled: true  # Đọc info.* từ application.yml
    build:
      enabled: true  # Thông tin build từ META-INF/build-info.properties
    git:
      enabled: true  # Thông tin git từ git.properties
      mode: full     # simple | full

  # ─── Metrics & Prometheus ────────────────────────────────────────────────
  metrics:
    tags:
      # Tags mặc định gắn vào tất cả metrics
      application: edu-backend
      environment: ${spring.profiles.active:dev}
    distribution:
      percentiles-histogram:
        http.server.requests: true  # Histogram cho HTTP latency
      percentiles:
        http.server.requests: 0.5, 0.90, 0.95, 0.99  # P50, P90, P95, P99
      slo:
        http.server.requests: 100ms, 200ms, 500ms, 1s  # Service Level Objectives
    enable:
      jvm: true      # JVM metrics (heap, GC, threads)
      process: true  # Process metrics (CPU, uptime)

# ─── Application Info (hiện thị tại /actuator/info) ──────────────────────
info:
  app:
    name: E-Edu Backend
    description: Hệ thống quản lý giáo dục trực tuyến
    version: '@project.version@'
  contact:
    team: E-Edu Development Team
```

---

## 4. Custom `HealthIndicator` — Kiểm tra Đa Schema PostgreSQL

### 4.1 Interface Overview

Spring Boot Actuator tự động gọi tất cả bean implement `HealthIndicator` khi truy cập `/actuator/health`.

### 4.2 `MultiSchemaHealthIndicator.java`

```java
package com.edu.boot.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom HealthIndicator kiểm tra kết nối và trạng thái của cả hai schema
 * {@code identity} và {@code organization} trong PostgreSQL.
 *
 * <p>Được tự động tích hợp vào {@code /actuator/health} endpoint với key
 * {@code "multiSchema"} (tên bean viết thường, bỏ hậu tố "HealthIndicator").
 *
 * <p>Response khi UP:
 * <pre>{@code
 * {
 *   "status": "UP",
 *   "components": {
 *     "multiSchema": {
 *       "status": "UP",
 *       "details": {
 *         "schema.identity": "UP (users: 42)",
 *         "schema.organization": "UP (organizations: 10)"
 *       }
 *     }
 *   }
 * }
 * }</pre>
 */
@Component("multiSchema")  // Key trong health response
public class MultiSchemaHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(MultiSchemaHealthIndicator.class);

    private final JdbcTemplate jdbcTemplate;

    public MultiSchemaHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        boolean allHealthy = true;

        // ─── Kiểm tra schema identity ─────────────────────────────────────
        try {
            Long userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM identity.users WHERE is_active = TRUE",
                    Long.class
            );
            details.put("schema.identity", "UP (active users: " + userCount + ")");
        } catch (Exception e) {
            log.error("[HealthCheck] Schema 'identity' check failed", e);
            details.put("schema.identity", "DOWN - " + e.getMessage());
            allHealthy = false;
        }

        // ─── Kiểm tra schema organization ────────────────────────────────
        try {
            Long orgCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM organization.organizations WHERE is_active = TRUE",
                    Long.class
            );
            details.put("schema.organization", "UP (active orgs: " + orgCount + ")");
        } catch (Exception e) {
            log.error("[HealthCheck] Schema 'organization' check failed", e);
            details.put("schema.organization", "DOWN - " + e.getMessage());
            allHealthy = false;
        }

        // ─── Kiểm tra Flyway migrations ───────────────────────────────────
        try {
            Integer successfulMigrations = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE",
                    Integer.class
            );
            details.put("flyway.migrations.applied", successfulMigrations);
        } catch (Exception e) {
            log.warn("[HealthCheck] Flyway history table check failed: {}", e.getMessage());
            details.put("flyway.migrations", "UNKNOWN");
        }

        if (allHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
}
```

### 4.3 Cấu hình Group Health Indicators (Kubernetes Probes)

```yaml
# application.yml
management:
  endpoint:
    health:
      group:
        # Liveness probe: chỉ cần app đang chạy
        liveness:
          include: ping
          show-details: never
        # Readiness probe: cần DB sẵn sàng
        readiness:
          include: db, multiSchema
          show-details: when-authorized
```

```
GET /actuator/health/liveness  → { "status": "UP" }
GET /actuator/health/readiness → { "status": "UP", "components": { ... } }
```

---

## 5. Custom Business Metrics với Micrometer

```java
package com.edu.identity.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

/**
 * Ví dụ tích hợp Micrometer custom metrics trong Application Service.
 */
@Service
public class UserApplicationService {

    // ─── Custom Counters ───────────────────────────────────────────────────
    private final Counter userRegistrationCounter;
    private final Counter userLoginSuccessCounter;
    private final Counter userLoginFailureCounter;

    // ─── Custom Timer ──────────────────────────────────────────────────────
    private final Timer userRegistrationTimer;

    public UserApplicationService(MeterRegistry meterRegistry) {
        // Counter: Số lần đăng ký thành công
        this.userRegistrationCounter = Counter.builder("edu.user.registration.total")
                .description("Tổng số lần đăng ký tài khoản thành công")
                .tag("application", "edu-backend")
                .register(meterRegistry);

        // Counter: Đăng nhập thành công
        this.userLoginSuccessCounter = Counter.builder("edu.user.login.total")
                .description("Tổng số lần đăng nhập")
                .tag("result", "success")
                .register(meterRegistry);

        // Counter: Đăng nhập thất bại
        this.userLoginFailureCounter = Counter.builder("edu.user.login.total")
                .description("Tổng số lần đăng nhập")
                .tag("result", "failure")
                .register(meterRegistry);

        // Timer: Thời gian xử lý đăng ký
        this.userRegistrationTimer = Timer.builder("edu.user.registration.duration")
                .description("Thời gian xử lý yêu cầu đăng ký")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public UserResponse register(RegisterUserRequest request) {
        return userRegistrationTimer.record(() -> {
            // ... business logic
            userRegistrationCounter.increment();
            return UserResponse.from(savedUser);
        });
    }
}
```

---

## 6. Bảo vệ Actuator Endpoints

### 6.1 Chiến lược bảo mật

| Endpoint | Quyền truy cập | Lý do |
|---|---|---|
| `/actuator/health` | **Public** | Cần cho load balancer, uptime monitor |
| `/actuator/info` | **Public** | Thông tin phiên bản, không nhạy cảm |
| `/actuator/prometheus` | **Internal only** (IP whitelist hoặc Internal network) | Prometheus server scrape |
| `/actuator/metrics` | **ROLE_ADMIN** | Thông tin kỹ thuật |
| `/actuator/loggers` | **ROLE_ADMIN** | Thay đổi log level — nhạy cảm |
| `/actuator/env` | **ROLE_ADMIN** | Có thể lộ credentials |
| `/actuator/heapdump` | **ROLE_ADMIN** | File lớn, có thể chứa sensitive data |
| `/actuator/threaddump` | **ROLE_ADMIN** | Debug only |
| `/actuator/flyway` | **ROLE_ADMIN** | Thông tin migration schema |

### 6.2 Cấu hình Spring Security cho Actuator

```java
package com.edu.boot.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình bảo mật riêng cho Actuator endpoints.
 *
 * <p>Sử dụng {@code @Order(1)} để SecurityFilterChain này có độ ưu tiên cao hơn
 * SecurityFilterChain chính của ứng dụng.
 */
@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig {

    /**
     * Security filter chain chuyên dụng cho actuator endpoints.
     * Order cao hơn (số nhỏ hơn) = ưu tiên cao hơn.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Chỉ áp dụng cho actuator endpoints
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                // Public: health và info không cần xác thực
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                .requestMatchers(EndpointRequest.to(InfoEndpoint.class)).permitAll()

                // Prometheus: chỉ cho phép từ internal network (cấu hình IP whitelist tại reverse proxy)
                // Hoặc yêu cầu ROLE_PROMETHEUS nếu cần auth
                .requestMatchers(EndpointRequest.to("prometheus")).permitAll()

                // Tất cả endpoint khác yêu cầu ROLE_ADMIN
                .anyRequest().hasRole("ADMIN")
            )
            // Actuator không cần CSRF (không có browser session)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
```

### 6.3 Cách tiếp cận Production với Nginx/API Gateway

```nginx
# nginx.conf — Chặn actuator ra khỏi public internet
server {
    listen 80;
    server_name api.edu-platform.com;

    # Block toàn bộ /actuator trừ health và info
    location /actuator/ {
        deny all;
    }

    # Cho phép health check từ load balancer
    location /actuator/health {
        allow 10.0.0.0/8;    # Internal network
        allow 172.16.0.0/12; # Docker/K8s network
        deny all;
    }

    # Prometheus scrape từ monitoring server
    location /actuator/prometheus {
        allow 10.100.0.10;   # Prometheus server IP
        deny all;
    }

    # Forward request đến Spring Boot
    location / {
        proxy_pass http://backend:8080;
    }
}
```

---

## 7. Prometheus Metrics Format

Khi truy cập `/actuator/prometheus`, response có dạng:

```
# HELP edu_user_registration_total Tổng số lần đăng ký tài khoản thành công
# TYPE edu_user_registration_total counter
edu_user_registration_total{application="edu-backend",environment="prod"} 1247.0

# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{exception="None",method="POST",outcome="SUCCESS",status="201",uri="/api/v1/users/register",le="0.1"} 1156
http_server_requests_seconds_bucket{exception="None",method="POST",outcome="SUCCESS",status="201",uri="/api/v1/users/register",le="0.2"} 1230

# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space"} 5.24288E7
```

---

## 8. Grafana Dashboard (Gợi ý)

### Panels cần thiết:

| Panel | Query PromQL | Mô tả |
|---|---|---|
| HTTP Request Rate | `rate(http_server_requests_seconds_count[5m])` | Requests/giây |
| HTTP Error Rate | `rate(http_server_requests_seconds_count{status=~"5.."}[5m])` | 5xx errors/giây |
| P95 Latency | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))` | Latency percentile |
| JVM Heap Usage | `jvm_memory_used_bytes{area="heap"}` | Heap memory đang dùng |
| Active DB Connections | `hikaricp_connections_active` | HikariCP connection pool |
| User Registration Rate | `rate(edu_user_registration_total[5m])` | Business metric |
| Application Uptime | `process_uptime_seconds` | Thời gian chạy |

---

## 9. Tiêu chí Nghiệm thu (Acceptance Criteria)

| Mã AC | Tiêu chí | Chi tiết kỹ thuật |
|:---:|---|---|
| **AC-3.5.1** | `/actuator/health` trả `{"status":"UP"}` | HTTP 200 khi DB healthy |
| **AC-3.5.2** | `/actuator/health` trả `{"status":"DOWN"}` khi DB lỗi | HTTP 503, component `multiSchema` DOWN |
| **AC-3.5.3** | Custom `multiSchemaHealthIndicator` hiển thị trong health | `components.multiSchema` có trong response |
| **AC-3.5.4** | `/actuator/prometheus` trả metrics Prometheus format | Content-Type `text/plain; version=0.0.4` |
| **AC-3.5.5** | JVM metrics có mặt | `jvm_memory_used_bytes`, `jvm_gc_*` có trong Prometheus output |
| **AC-3.5.6** | HTTP metrics có mặt | `http_server_requests_seconds_*` có trong Prometheus output |
| **AC-3.5.7** | `/actuator/health` public, không cần auth | `curl /actuator/health` → 200 mà không có header Authorization |
| **AC-3.5.8** | `/actuator/env` yêu cầu ROLE_ADMIN | `curl /actuator/env` → 401/403 khi chưa xác thực |
| **AC-3.5.9** | Kubernetes probes hoạt động | `GET /actuator/health/liveness` → 200, `GET /actuator/health/readiness` → 200 khi DB ready |
| **AC-3.5.10** | Custom business counters được ghi nhận | `edu_user_registration_total` tăng mỗi lần register thành công |

---

## 10. Liên kết Tài liệu Liên quan

| Tài liệu | Mô tả |
|---|---|
| [Phase 3.4 — Centralized Logging](phase3.4.md) | `TraceIdFilter` bổ sung `traceId` context cho metrics/logs |
| [Phase 3.3 — Flyway Migrations](phase3.3.md) | `/actuator/flyway` endpoint hiển thị trạng thái migration |
| [Phase 3 README](README.md) | Index tổng thể Phase 3 |
