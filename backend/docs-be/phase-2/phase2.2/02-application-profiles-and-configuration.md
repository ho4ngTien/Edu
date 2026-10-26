# Phase 2.2 — Thành phần 02: Application Profiles & Configuration Hierarchy

Tài liệu này đặc tả quy chuẩn tổ chức các file cấu hình YAML và cơ chế phân tầng Profile trong Spring Boot.

---

## 1. Cấu Trúc Các Tệp YAML Cấu Hình

```
boot/src/main/resources/
├── application.yml              # Cấu hình gốc mặc định
├── application-local.yml        # Cấu hình môi trường dev máy cá nhân
├── application-test.yml         # Cấu hình phục vụ chạy Unit & Integration Test
└── application-prod.yml         # Cấu hình môi trường Production
```

---

## 2. Chi Tiết Từng File Cấu Hình

### 2.1. `application.yml` (Cấu hình mặc định)
```yaml
spring:
  application:
    name: edu-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/edu}
    username: ${DB_USERNAME:edu_user}
    password: ${DB_PASSWORD:edu_secret}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        jdbc:
          batch_size: 25
```

### 2.2. `application-local.yml` (Môi trường phát triển Local)
```yaml
logging:
  level:
    root: INFO
    com.edu: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### 2.3. `application-test.yml` (Môi trường kiểm thử Test)
```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```
